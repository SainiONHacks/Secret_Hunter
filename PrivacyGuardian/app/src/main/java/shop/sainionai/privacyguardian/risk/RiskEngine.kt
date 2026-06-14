package shop.sainionai.privacyguardian.risk

import shop.sainionai.privacyguardian.model.PermissionCategory
import shop.sainionai.privacyguardian.model.RiskComponent
import shop.sainionai.privacyguardian.model.RiskLevel
import shop.sainionai.privacyguardian.model.RiskScore
import shop.sainionai.privacyguardian.model.ScannedPermission
import shop.sainionai.privacyguardian.scanner.PermissionClassifier

/**
 * Privacy Guardian Risk Engine.
 *
 * Design goals (from the review of the product plan):
 *  - VERSIONED: ENGINE_VERSION ships with every score and report.
 *  - REPRODUCIBLE: pure function of inputs, no randomness, no hidden state.
 *  - TRANSPARENT: returns every component + a human-readable rationale.
 *  - HONEST: signals that aren't implemented yet are marked unassessed and
 *    excluded from the blend (lowering confidence) instead of silently scoring 0.
 *
 * Overall = weighted average over ASSESSED components only.
 * confidence = (sum of assessed weights) / (sum of all weights).
 */
object RiskEngine {

    const val ENGINE_VERSION = "1.3.0"

    // Component weights. They sum to 1.0 across the full (future) feature set.
    private const val W_PERMISSION = 0.40
    private const val W_NETWORK    = 0.25  // Phase 2 (VPN) — unassessed in MVP
    private const val W_REPUTATION = 0.20  // Phase 6 (threat intel) — unassessed in MVP
    private const val W_BEHAVIOR   = 0.15  // Phase 3 (evidence/runtime) — unassessed in MVP

    // --- Permission-curve tuning (validated; change ⇒ bump ENGINE_VERSION) ---
    private const val SOFT_OR_FACTOR = 0.85
    private const val RAW_SCALE      = 0.80
    private const val BONUS_SCALE    = 0.60

    fun score(
        permissions: List<ScannedPermission>,
        trackerCount: Int = 0,
        reputationValue: Int? = null,
        reputationNote: String? = null
    ): RiskScore {
        val permission = permissionComponent(permissions)

        val network = RiskComponent(
            name = "Network",
            value = 0,
            weight = W_NETWORK,
            assessed = false,
            rationale = "Not assessed in MVP. Requires Phase 2 live capture."
        )
        val reputation = RiskComponent(
            name = "Reputation",
            value = reputationValue ?: 0,
            weight = W_REPUTATION,
            assessed = reputationValue != null,
            rationale = when {
                reputationValue == null -> "No known-risk indicator matched (unknown, not 'safe')."
                else -> reputationNote ?: "Matched a known-risk indicator."
            }
        )
        val behavior = behaviorComponent(trackerCount)

        val components = listOf(permission, network, reputation, behavior)
        val assessed = components.filter { it.assessed }

        val assessedWeight = assessed.sumOf { it.weight }
        val overall = if (assessedWeight == 0.0) 0 else
            (assessed.sumOf { it.value * it.weight } / assessedWeight).toInt()

        val totalWeight = components.sumOf { it.weight }
        val confidence = if (totalWeight == 0.0) 0.0 else assessedWeight / totalWeight

        return RiskScore(
            overall = overall.coerceIn(0, 100),
            level = RiskLevel.fromScore(overall),
            confidence = confidence,
            components = components,
            engineVersion = ENGINE_VERSION
        )
    }

    /**
     * Permission sub-score. Granted sensitive permissions count fully; merely
     * declared-but-not-granted ones count at 40% (intent signal, not active access).
     * Dangerous category combinations add a bonus on top.
     */
    private fun permissionComponent(permissions: List<ScannedPermission>): RiskComponent {
        if (permissions.isEmpty()) {
            return RiskComponent("Permissions", 0, W_PERMISSION, true,
                "No permissions declared.")
        }

        // Saturating "soft-OR" aggregation (validated against a test matrix):
        // no single permission can peg the score, but several invasive ones stack.
        // Common-sensitive perms (camera/photo/location) settle in MEDIUM; SMS /
        // call-log reach HIGH/CRITICAL. See RiskEngineTest for pinned expectations.
        val sensitive = permissions.filter { it.basePoints >= 30 }
        if (sensitive.isEmpty()) {
            return RiskComponent("Permissions", 0, W_PERMISSION, true,
                "No sensitive permissions declared.")
        }

        var product = 1.0
        for (p in sensitive.sortedByDescending { effectivePoints(it) }) {
            val pts = effectivePoints(p).coerceIn(0.0, 100.0)
            product *= (1.0 - (pts / 100.0) * SOFT_OR_FACTOR)
        }
        var raw = 100.0 * (1.0 - product) * RAW_SCALE

        // Combo bonuses — the patterns that distinguish abusive apps. Scaled so a
        // single combo nudges rather than pegs the score.
        val granted = permissions.filter { it.granted }.map { it.category }.toSet()
        val bonuses = comboBonuses(granted)
        raw = (raw + bonuses.sumOf { it.second } * BONUS_SCALE).coerceAtMost(100.0)

        val rationale = buildString {
            append("${sensitive.count { it.granted }} sensitive permission(s) granted")
            if (bonuses.isNotEmpty()) {
                append("; flagged combos: ")
                append(bonuses.joinToString(", ") { it.first })
            }
            append(".")
        }

        return RiskComponent("Permissions", raw.toInt(), W_PERMISSION, true, rationale)
    }

    /**
     * Effective privacy points for a permission. Granted perms count fully, declared-only
     * at 40%. Category-aware modifier (v1.3): a sensitive permission that is UNEXPECTED for
     * the app's category weighs 1.3x; an EXPECTED one weighs 0.62x. This is what separates a
     * messenger reading SMS (expected) from a flashlight reading SMS (unexpected).
     */
    private fun effectivePoints(p: ScannedPermission): Double {
        var v = if (p.granted) p.basePoints.toDouble() else p.basePoints * 0.4
        if (p.basePoints >= 30) v *= if (p.expected) 0.62 else 1.30
        return v
    }

    /**
     * Behavior signal — in v1.1 this is driven by embedded tracker SDK count from
     * static analysis. (Runtime behavior from Phase 3 will refine this later.)
     */
    private fun behaviorComponent(trackerCount: Int): RiskComponent {
        val value = when {
            trackerCount <= 0 -> 0
            trackerCount <= 2 -> 30
            trackerCount <= 4 -> 55
            trackerCount <= 7 -> 75
            else -> 90
        }
        return RiskComponent(
            name = "Behavior",
            value = value,
            weight = W_BEHAVIOR,
            assessed = true,
            rationale = if (trackerCount == 0) "No known tracker SDKs detected."
            else "$trackerCount tracker SDK(s) embedded (static analysis)."
        )
    }

    /** Returns (label, bonusPoints) for each risky category combination present. */
    private fun comboBonuses(granted: Set<PermissionCategory>): List<Pair<String, Int>> {
        val out = mutableListOf<Pair<String, Int>>()
        fun has(vararg c: PermissionCategory) = c.all { it in granted }

        if (has(PermissionCategory.SMS, PermissionCategory.CONTACTS))
            out += "SMS + Contacts (data harvesting)" to 15
        if (has(PermissionCategory.SMS, PermissionCategory.NETWORK))
            out += "SMS + Network (OTP/data exfil risk)" to 12
        if (has(PermissionCategory.CALL_LOG, PermissionCategory.CONTACTS))
            out += "Call Log + Contacts (social graph)" to 12
        if (has(PermissionCategory.LOCATION, PermissionCategory.NETWORK) &&
            granted.contains(PermissionCategory.CONTACTS))
            out += "Location + Contacts + Network" to 8
        if (has(PermissionCategory.MICROPHONE, PermissionCategory.NETWORK))
            out += "Microphone + Network" to 8
        return out
    }
}
