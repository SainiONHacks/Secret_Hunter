package shop.sainionai.privacyguardian.model

enum class ChangeType { NEW_APP, REMOVED_APP, RISK_INCREASED, NEW_SENSITIVE_PERMISSION, NEW_TRACKERS }

data class AppChange(
    val packageName: String,
    val label: String,
    val type: ChangeType,
    val detail: String
)

data class ScanDiff(
    val changes: List<AppChange>,
    val previousTimestamp: Long?,
    val currentTimestamp: Long?
) {
    val isEmpty: Boolean get() = changes.isEmpty()
    val hasPrevious: Boolean get() = previousTimestamp != null

    /** Changes worth a notification: new high/critical apps, or new sensitive access. */
    val notable: List<AppChange> get() = changes.filter {
        it.type == ChangeType.NEW_APP || it.type == ChangeType.RISK_INCREASED ||
            it.type == ChangeType.NEW_SENSITIVE_PERMISSION
    }
}
