package shop.sainionai.privacyguardian.model

/**
 * Category a sensitive permission belongs to. Drives both the UI grouping
 * and the weighting in [shop.sainionai.privacyguardian.risk.RiskEngine].
 */
enum class PermissionCategory(val label: String) {
    CONTACTS("Contacts"),
    SMS("SMS"),
    CALL_LOG("Call Log"),
    PHONE("Phone"),
    LOCATION("Location"),
    CAMERA("Camera"),
    MICROPHONE("Microphone"),
    STORAGE("Storage / Files"),
    GALLERY("Photos / Media"),
    CALENDAR("Calendar"),
    SENSORS("Body Sensors"),
    NETWORK("Network"),
    OTHER("Other")
}

/**
 * A single permission discovered on an installed app, already classified.
 *
 * @param androidName the raw permission string, e.g. android.permission.READ_SMS
 * @param granted whether the permission is currently granted (runtime state)
 * @param category coarse grouping for UI + scoring
 * @param basePoints contribution to the permission-risk sub-score (0..100 scale,
 *        before combo bonuses). Documented and versioned so scores are reproducible.
 */
data class ScannedPermission(
    val androidName: String,
    val granted: Boolean,
    val category: PermissionCategory,
    val basePoints: Int,
    val expected: Boolean = true
) {
    val shortName: String get() = androidName.substringAfterLast('.')
}
