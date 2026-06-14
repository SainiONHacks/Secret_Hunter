package shop.sainionai.privacyguardian.network

/**
 * Minimal DNS query-name extractor (Phase 2 hostname signal).
 *
 * Parses the QNAME from an outbound DNS query so the monitor can show "the device
 * asked for api.example.com" even when we only see one direction of traffic. Query
 * parsing only (no compression pointers, which don't appear in a question QNAME).
 */
object DnsParser {

    /**
     * @param payload the full IP packet bytes
     * @param dnsStart offset where the DNS message begins (after IP+UDP headers)
     * @return the queried hostname, or null if not a parseable query
     */
    fun parseQueryName(payload: ByteArray, dnsStart: Int): String? {
        var i = dnsStart + 12                      // skip 12-byte DNS header
        if (i >= payload.size) return null
        val labels = ArrayList<String>(8)
        var guard = 0
        while (i < payload.size) {
            val len = payload[i].toInt() and 0xFF; i++
            if (len == 0) break
            if (len and 0xC0 != 0) return null       // compression pointer: bail
            if (i + len > payload.size) return null
            labels.add(String(payload, i, len, Charsets.US_ASCII)); i += len
            if (++guard > 127) return null
        }
        return if (labels.isEmpty()) null else labels.joinToString(".")
    }
}
