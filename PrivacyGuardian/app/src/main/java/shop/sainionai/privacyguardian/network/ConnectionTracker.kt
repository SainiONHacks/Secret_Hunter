package shop.sainionai.privacyguardian.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import shop.sainionai.privacyguardian.model.Destination
import shop.sainionai.privacyguardian.model.GeoInfo
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory aggregation of observed connections, grouped by destination IP.
 * Fed by the VPN service; read by the network-monitor UI. Process-scoped singleton.
 */
object ConnectionTracker {

    private data class Agg(
        var hostname: String?,
        val ip: String,
        var bytesOut: Long = 0,
        var bytesIn: Long = 0,
        var connections: Int = 0,
        var geo: GeoInfo? = null
    )

    private val map = ConcurrentHashMap<String, Agg>()
    private val _destinations = MutableStateFlow<List<Destination>>(emptyList())
    val destinations: StateFlow<List<Destination>> = _destinations

    /** Record one observed flow event. Called from the VPN read loop. */
    fun record(ip: String, hostname: String?, out: Long, inn: Long, geo: GeoInfo?) {
        val a = map.getOrPut(ip) { Agg(hostname, ip, geo = geo) }
        if (hostname != null) a.hostname = hostname
        if (geo != null) a.geo = geo
        a.bytesOut += out
        a.bytesIn += inn
        a.connections += 1
        publish()
    }

    private fun publish() {
        _destinations.value = map.values
            .map { Destination(it.hostname, it.ip, it.bytesOut, it.bytesIn, it.connections, it.geo) }
            .sortedByDescending { it.bytesOut }
    }

    fun reset() { map.clear(); _destinations.value = emptyList() }
}
