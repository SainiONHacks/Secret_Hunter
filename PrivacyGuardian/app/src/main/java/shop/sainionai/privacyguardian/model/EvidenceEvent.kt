package shop.sainionai.privacyguardian.model

/** Kinds of evidence event the system can record. Ordered roughly by escalation. */
enum class EvidenceType(val label: String, val severity: Int) {
    APP_OPENED("App opened", 1),
    PERMISSION_ACCESSED("Sensitive permission active", 2),
    NETWORK_CONNECTION("Network connection", 2),
    UPLOAD_DETECTED("Upload detected", 3),
    CANARY_ACCESSED("Canary resource accessed", 4),
    REPUTATION_HIT("Matched known-risk indicator", 4)
}

/**
 * A single timestamped evidence record. Deliberately neutral wording — these are
 * observed events, not accusations. Correlation across events is what builds a case.
 */
data class EvidenceEvent(
    val timestamp: Long,
    val packageName: String,
    val type: EvidenceType,
    val detail: String
)
