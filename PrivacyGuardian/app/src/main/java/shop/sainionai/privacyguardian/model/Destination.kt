package shop.sainionai.privacyguardian.model

/** Server-location info resolved offline from an IP. Describes the *server*, not the user. */
data class GeoInfo(
    val countryCode: String,
    val city: String,
    val org: String
) {
    val isCdn: Boolean get() = org.contains("Cloudflare", true) ||
            org.contains("Google", true) || org.contains("Akamai", true) ||
            org.contains("Fastly", true)
}

/** Aggregated traffic to one destination for one monitored app. */
data class Destination(
    val hostname: String?,      // from SNI/DNS when available; may be null (IP-only)
    val ip: String,
    val bytesOut: Long,
    val bytesIn: Long,
    val connections: Int,
    val geo: GeoInfo?
) {
    val label: String get() = hostname ?: ip
}
