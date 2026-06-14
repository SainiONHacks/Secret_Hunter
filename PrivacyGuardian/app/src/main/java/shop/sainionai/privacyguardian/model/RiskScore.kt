package shop.sainionai.privacyguardian.model

enum class RiskLevel(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");

    companion object {
        /** Fixed, versioned thresholds. Part of RiskEngine v1 contract. */
        fun fromScore(score: Int): RiskLevel = when {
            score >= 80 -> CRITICAL
            score >= 55 -> HIGH
            score >= 30 -> MEDIUM
            else -> LOW
        }
    }
}

/**
 * One weighted input to the overall score.
 *
 * @param value normalised 0..100 sub-score
 * @param weight relative weight in the overall blend
 * @param assessed false when this signal is not yet implemented (e.g. network
 *        risk before Phase 2). Unassessed components are EXCLUDED from the blend
 *        rather than counted as zero, and they lower [RiskScore.confidence].
 *        This keeps the MVP score honest instead of falsely reassuring.
 */
data class RiskComponent(
    val name: String,
    val value: Int,
    val weight: Double,
    val assessed: Boolean,
    val rationale: String
)

/**
 * Transparent, reproducible risk result.
 *
 * @param engineVersion semantic version of the scoring logic that produced this.
 *        Persisted with reports so a score can always be traced to its algorithm.
 * @param confidence fraction (0..1) of total weight that was actually assessed.
 */
data class RiskScore(
    val overall: Int,
    val level: RiskLevel,
    val confidence: Double,
    val components: List<RiskComponent>,
    val engineVersion: String
)
