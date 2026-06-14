package shop.sainionai.privacyguardian.model

/**
 * Result of scanning one installed application.
 *
 * Everything here is derived locally from PackageManager. No app data is read,
 * no network call is made — consistent with the offline-first mission.
 */
data class ScannedApp(
    val packageName: String,
    val appLabel: String,
    val versionName: String?,
    val isSystemApp: Boolean,
    val installerPackage: String?,
    val permissions: List<ScannedPermission>,
    val trackers: List<DetectedTracker>,
    val risk: RiskScore
) {
    /** Sensitive permissions actually granted right now — the headline evidence. */
    val grantedSensitive: List<ScannedPermission>
        get() = permissions.filter { it.granted && it.category != PermissionCategory.OTHER }
}
