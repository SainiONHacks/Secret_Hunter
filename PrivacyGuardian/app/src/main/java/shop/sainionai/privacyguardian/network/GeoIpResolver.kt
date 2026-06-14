package shop.sainionai.privacyguardian.network

import android.content.Context
import shop.sainionai.privacyguardian.model.GeoInfo
import java.io.BufferedReader

/**
 * Offline IP -> server-location resolver.
 *
 * Loads a CSV of IP ranges from assets (assets/geoip_ranges.csv) into a sorted array
 * and resolves with binary search. Fully on-device — looking up IPs via an online API
 * would mean this privacy app phones out, which we refuse to do.
 *
 * CSV columns: start_ip_long,end_ip_long,country_code,city,org   (one range per line)
 * For production, generate this from DB-IP Lite or IP2Location Lite (both free,
 * redistribution-friendly) and bump the bundled DB version.
 *
 * IMPORTANT: GeoIP locates the *server / hosting provider*, not where a user's data
 * legally resides. Surface it as "connects to a server in X", never "your data went to X".
 */
class GeoIpResolver private constructor(
    private val starts: LongArray,
    private val ends: LongArray,
    private val cc: Array<String>,
    private val city: Array<String>,
    private val org: Array<String>
) {

    fun resolve(ip: String): GeoInfo? {
        val x = ipToLong(ip) ?: return null
        var lo = 0; var hi = starts.size - 1; var idx = -1
        while (lo <= hi) {                       // rightmost start <= x
            val mid = (lo + hi) ushr 1
            if (starts[mid] <= x) { idx = mid; lo = mid + 1 } else hi = mid - 1
        }
        if (idx < 0 || x > ends[idx]) return null
        return GeoInfo(cc[idx], city[idx], org[idx])
    }

    companion object {
        fun fromAssets(context: Context, asset: String = "geoip_ranges.csv"): GeoIpResolver =
            runCatching {
                context.assets.open(asset).bufferedReader().use { parse(it) }
            }.getOrElse { empty() }

        private fun parse(reader: BufferedReader): GeoIpResolver {
            data class Row(val s: Long, val e: Long, val cc: String, val city: String, val org: String)
            val rows = reader.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .mapNotNull { line ->
                    val p = line.split(",")
                    if (p.size < 5) return@mapNotNull null
                    val s = p[0].toLongOrNull() ?: return@mapNotNull null
                    val e = p[1].toLongOrNull() ?: return@mapNotNull null
                    Row(s, e, p[2], p[3], p.subList(4, p.size).joinToString(","))
                }
                .sortedBy { it.s }
                .toList()
            return GeoIpResolver(
                starts = LongArray(rows.size) { rows[it].s },
                ends = LongArray(rows.size) { rows[it].e },
                cc = Array(rows.size) { rows[it].cc },
                city = Array(rows.size) { rows[it].city },
                org = Array(rows.size) { rows[it].org }
            )
        }

        private fun empty() =
            GeoIpResolver(LongArray(0), LongArray(0), emptyArray(), emptyArray(), emptyArray())

        fun ipToLong(ip: String): Long? {
            val parts = ip.split(".")
            if (parts.size != 4) return null            // IPv4 only in MVP
            var v = 0L
            for (p in parts) {
                val n = p.toIntOrNull() ?: return null
                if (n !in 0..255) return null
                v = (v shl 8) or n.toLong()
            }
            return v
        }
    }
}
