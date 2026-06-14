package shop.sainionai.privacyguardian.scanner

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import shop.sainionai.privacyguardian.model.AppCategory
import shop.sainionai.privacyguardian.model.CategoryExpectations
import shop.sainionai.privacyguardian.model.ScannedApp
import shop.sainionai.privacyguardian.model.ScannedPermission
import shop.sainionai.privacyguardian.reputation.ReputationDb
import shop.sainionai.privacyguardian.risk.RiskEngine

/**
 * Phase 1 Scanner Engine.
 *
 * Pure read-only inspection via PackageManager:
 *   app discovery -> requested permissions -> runtime granted state -> risk score.
 *
 * No app *data* is ever read here; only metadata the OS already exposes to any app.
 */
class ScannerEngine(private val context: Context) {

    private val pm: PackageManager get() = context.packageManager
    private val trackerDetector = TrackerDetector(context.packageManager)
    private val reputation = ReputationDb.fromAssets(context.applicationContext)

    /**
     * Scan installed apps.
     * @param includeSystem when false (default) system apps are skipped to cut noise.
     */
    suspend fun scanInstalledApps(includeSystem: Boolean = false): List<ScannedApp> =
        withContext(Dispatchers.Default) {
            val flags = PackageManager.GET_PERMISSIONS
            val packages: List<PackageInfo> =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getInstalledPackages(
                        PackageManager.PackageInfoFlags.of(flags.toLong())
                    )
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstalledPackages(flags)
                }

            packages.asSequence()
                .filter { includeSystem || !it.isSystem() }
                .filter { it.packageName != context.packageName } // never scan self
                .mapNotNull { runCatching { toScannedApp(it) }.getOrNull() }
                .sortedByDescending { it.risk.overall }
                .toList()
        }

    /**
     * Heuristic for the QUERY_ALL_PACKAGES-denied case. On API 30+, without the
     * permission, getInstalledPackages returns only packages visible to us — usually
     * a handful. If the non-system count is implausibly low, visibility is likely
     * restricted and the UI should nudge the user. This lets the Play build degrade
     * gracefully instead of silently showing a near-empty list.
     */
    suspend fun visibilityLooksRestricted(): Boolean = withContext(Dispatchers.Default) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return@withContext false
        val nonSystem = runCatching {
            val pkgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0L))
            else { @Suppress("DEPRECATION") pm.getInstalledPackages(0) }
            pkgs.count { !it.isSystem() }
        }.getOrDefault(0)
        nonSystem in 1..3
    }

    private fun toScannedApp(info: PackageInfo): ScannedApp {
        val appInfo = info.applicationInfo
        val label = appInfo?.let { pm.getApplicationLabel(it).toString() } ?: info.packageName

        val requested = info.requestedPermissions ?: emptyArray()
        val flagsArr = info.requestedPermissionsFlags ?: IntArray(requested.size)

        val appCategory = appInfo?.let { AppCategory.fromAndroid(it.category) } ?: AppCategory.UNKNOWN

        val permissions: List<ScannedPermission> = requested.mapIndexed { i, name ->
            val granted = (flagsArr.getOrElse(i) { 0 } and
                    PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            val permCategory = PermissionClassifier.categoryOf(name)
            ScannedPermission(
                androidName = name,
                granted = granted,
                category = permCategory,
                basePoints = PermissionClassifier.basePointsOf(name),
                expected = CategoryExpectations.isExpected(appCategory, permCategory)
            )
        }

        val trackers = trackerDetector.detect(info.packageName)
        val rep = reputation.forPackage(info.packageName)

        return ScannedApp(
            packageName = info.packageName,
            appLabel = label,
            versionName = info.versionName,
            isSystemApp = info.isSystem(),
            installerPackage = installerOf(info.packageName),
            permissions = permissions,
            trackers = trackers,
            risk = RiskEngine.score(
                permissions = permissions,
                trackerCount = trackers.size,
                reputationValue = rep?.risk,
                reputationNote = rep?.let { "Known-risk match: ${it.category}." }
            )
        )
    }

    private fun installerOf(pkg: String): String? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            pm.getInstallSourceInfo(pkg).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            pm.getInstallerPackageName(pkg)
        }
    }.getOrNull()

    private fun PackageInfo.isSystem(): Boolean {
        val ai = applicationInfo ?: return false
        return (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }
}
