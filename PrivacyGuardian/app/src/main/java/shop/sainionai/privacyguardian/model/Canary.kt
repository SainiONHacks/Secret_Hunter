package shop.sainionai.privacyguardian.model

enum class CanaryType { CONTACT, IMAGE, FILE }

/**
 * A decoy resource planted to detect access. On a non-root device we can observe
 * that a monitored resource was *touched* (via ContentObserver), but not reliably
 * which app touched it — so a canary access is a signal, not an attribution.
 */
data class Canary(
    val id: String,
    val type: CanaryType,
    val displayName: String,
    val locationHint: String,   // contact lookup key / file path
    val createdAt: Long
)
