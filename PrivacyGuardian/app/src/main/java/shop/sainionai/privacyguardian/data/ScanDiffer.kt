package shop.sainionai.privacyguardian.data

import shop.sainionai.privacyguardian.model.AppChange
import shop.sainionai.privacyguardian.model.ChangeType
import shop.sainionai.privacyguardian.model.RiskLevel
import shop.sainionai.privacyguardian.model.ScanDiff
import shop.sainionai.privacyguardian.model.ScannedApp

/** Computes what changed between two scans — the basis for monitoring + alerts. */
object ScanDiffer {

    fun diff(
        previous: List<ScannedApp>?,
        current: List<ScannedApp>,
        previousTs: Long? = null,
        currentTs: Long? = null
    ): ScanDiff {
        if (previous == null) return ScanDiff(emptyList(), null, currentTs)

        val prevByPkg = previous.associateBy { it.packageName }
        val curByPkg = current.associateBy { it.packageName }
        val changes = mutableListOf<AppChange>()

        // New apps
        for (app in current) {
            if (app.packageName !in prevByPkg) {
                changes += AppChange(app.packageName, app.appLabel, ChangeType.NEW_APP,
                    "Installed — risk ${app.risk.overall} (${app.risk.level.label})")
            }
        }
        // Removed apps
        for (app in previous) {
            if (app.packageName !in curByPkg) {
                changes += AppChange(app.packageName, app.appLabel, ChangeType.REMOVED_APP, "Uninstalled")
            }
        }
        // Changed apps
        for (app in current) {
            val before = prevByPkg[app.packageName] ?: continue

            if (rank(app.risk.level) > rank(before.risk.level)) {
                changes += AppChange(app.packageName, app.appLabel, ChangeType.RISK_INCREASED,
                    "${before.risk.level.label} → ${app.risk.level.label}")
            }

            val beforeGranted = before.grantedSensitive.map { it.androidName }.toSet()
            val newlyGranted = app.grantedSensitive.filter { it.androidName !in beforeGranted }
            if (newlyGranted.isNotEmpty()) {
                changes += AppChange(app.packageName, app.appLabel, ChangeType.NEW_SENSITIVE_PERMISSION,
                    "Gained: " + newlyGranted.joinToString(", ") { it.shortName })
            }

            if (app.trackers.size > before.trackers.size) {
                changes += AppChange(app.packageName, app.appLabel, ChangeType.NEW_TRACKERS,
                    "Trackers ${before.trackers.size} → ${app.trackers.size}")
            }
        }
        return ScanDiff(changes, previousTs, currentTs)
    }

    private fun rank(level: RiskLevel) = when (level) {
        RiskLevel.LOW -> 0; RiskLevel.MEDIUM -> 1; RiskLevel.HIGH -> 2; RiskLevel.CRITICAL -> 3
    }
}
