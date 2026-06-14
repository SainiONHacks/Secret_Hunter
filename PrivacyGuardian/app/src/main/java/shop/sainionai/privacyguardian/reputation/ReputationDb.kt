package shop.sainionai.privacyguardian.reputation

import android.content.Context

/**
 * Offline-first reputation database (Phase 6).
 *
 * Loads a bundled CSV of known-risk packages/domains. Lookup is local — no query
 * leaves the device. A known-bad match is a strong positive signal; the ABSENCE of a
 * match means "unknown", never "safe" (so callers leave reputation unassessed on a miss).
 *
 * Hybrid mode: [updateFromFeed] is the single, documented seam where an opt-in online
 * refresh would merge a downloaded list. It is a stub by default — the app ships fully
 * functional offline, and any online update must be explicit user choice.
 */
class ReputationDb private constructor(
    private val packages: Map<String, Verdict>,
    private val domains: Map<String, Verdict>
) {
    data class Verdict(val category: String, val risk: Int)

    fun forPackage(pkg: String): Verdict? = packages[pkg]
    fun forDomain(domain: String): Verdict? = domains[domain.lowercase()]

    /** Stub: in hybrid mode, merge a downloaded list here. No network call by default. */
    fun updateFromFeed(@Suppress("UNUSED_PARAMETER") csv: String): ReputationDb = this

    companion object {
        const val DB_VERSION = "1.0.0-bundled"

        fun fromAssets(context: Context, asset: String = "reputation.csv"): ReputationDb =
            runCatching {
                context.assets.open(asset).bufferedReader().useLines { lines ->
                    val pkg = HashMap<String, Verdict>()
                    val dom = HashMap<String, Verdict>()
                    lines.map { it.trim() }
                        .filter { it.isNotEmpty() && !it.startsWith("#") }
                        .forEach { line ->
                            val p = line.split(",")
                            if (p.size >= 4) {
                                val v = Verdict(p[2], p[3].toIntOrNull() ?: 0)
                                when (p[0]) {
                                    "package" -> pkg[p[1]] = v
                                    "domain" -> dom[p[1].lowercase()] = v
                                }
                            }
                        }
                    ReputationDb(pkg, dom)
                }
            }.getOrElse { ReputationDb(emptyMap(), emptyMap()) }
    }
}
