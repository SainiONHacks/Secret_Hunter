package shop.sainionai.privacyguardian.network

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import shop.sainionai.privacyguardian.evidence.EvidenceRecorder
import shop.sainionai.privacyguardian.model.EvidenceType

/**
 * Phase 2 local VPN monitor — SKELETON.
 *
 * What is REAL here:
 *  - Correct VpnService lifecycle + tun interface setup via Builder.
 *  - A read loop that pulls IP packets off the tun fd.
 *  - Destination-IP extraction from the IPv4 header (offset 16..19).
 *  - GeoIP decoration + aggregation via ConnectionTracker.
 *
 * What is INTENTIONALLY STUBBED (do not ship as-is):
 *  - Full TCP/UDP reassembly and a userspace TCP/IP stack to actually forward
 *    traffic. Without forwarding, enabling this VPN will BLACKHOLE connectivity.
 *    A production build needs a tun2socks-style forwarder (e.g. the approach used
 *    by NetGuard / RethinkDNS). That's a large sub-project; it's flagged, not faked.
 *  - SNI/DNS hostname extraction (here hostname stays null -> UI shows the IP).
 *  - Per-app attribution via getConnectionOwnerUid() (API 29+); marked below.
 *
 * Because of the above, this service is here to make the architecture concrete and
 * compile cleanly — not to be turned on in production yet.
 */
class PrivacyVpnService : VpnService() {

    private var tun: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var loop: Job? = null

    private lateinit var geo: GeoIpResolver

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        geo = GeoIpResolver.fromAssets(applicationContext)
        if (intent?.action == ACTION_STOP) { stopMonitoring(); return START_NOT_STICKY }
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        if (tun != null) return
        tun = Builder()
            .setSession("Privacy Guardian")
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)          // capture all IPv4
            .setBlocking(true)
            .establish() ?: return

        loop = scope.launch { readLoop(tun!!) }
    }

    private suspend fun readLoop(pfd: ParcelFileDescriptor) {
        val input = FileInputStream(pfd.fileDescriptor)
        val buf = ByteArray(32_767)
        while (currentCoroutineScopeActive()) {
            val n = runCatching { input.read(buf) }.getOrDefault(-1)
            if (n <= 0) continue
            handlePacket(buf, n)
            // NOTE: no forwarding here — see class doc. Production must forward `buf`.
        }
    }

    /** Extract destination IP (and DNS hostname when present) from an IPv4 packet. */
    private fun handlePacket(packet: ByteArray, len: Int) {
        if (len < 20) return
        val version = (packet[0].toInt() ushr 4) and 0xF
        if (version != 4) return                       // IPv4 only in MVP
        val ihl = (packet[0].toInt() and 0x0F) * 4     // IP header length
        val protocol = packet[9].toInt() and 0xFF      // 6=TCP, 17=UDP
        val dst = "%d.%d.%d.%d".format(
            packet[16].toInt() and 0xFF, packet[17].toInt() and 0xFF,
            packet[18].toInt() and 0xFF, packet[19].toInt() and 0xFF
        )

        // Hostname signal: parse QNAME from outbound DNS (UDP/53) queries.
        var hostname: String? = null
        if (protocol == 17 && len >= ihl + 8) {
            val dstPort = ((packet[ihl + 2].toInt() and 0xFF) shl 8) or (packet[ihl + 3].toInt() and 0xFF)
            if (dstPort == 53) {
                hostname = DnsParser.parseQueryName(packet, ihl + 8)
            }
        }

        val geo = geo.resolve(dst)
        ConnectionTracker.record(ip = dst, hostname = hostname, out = len.toLong(), inn = 0, geo = geo)

        // Evidence: record the connection (and DNS lookup) on the timeline.
        // Per-app attribution via getConnectionOwnerUid() (API 29+) would resolve the
        // owning uid->package here; best-effort and racy without root, so left as a
        // documented hook rather than a false certainty.
        val recorder = EvidenceRecorder.get(applicationContext)
        if (hostname != null) {
            recorder.record("unknown", EvidenceType.NETWORK_CONNECTION, "DNS lookup: $hostname")
        }
    }

    private fun currentCoroutineScopeActive(): Boolean = scope.isActive

    private fun stopMonitoring() {
        loop?.cancel(); loop = null
        runCatching { tun?.close() }; tun = null
    }

    override fun onDestroy() {
        stopMonitoring()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "shop.sainionai.privacyguardian.STOP_VPN"

        /** Returns an intent to launch the system VPN-consent dialog, or null if already granted. */
        fun consentIntent(context: Context): Intent? = prepare(context)
    }
}
