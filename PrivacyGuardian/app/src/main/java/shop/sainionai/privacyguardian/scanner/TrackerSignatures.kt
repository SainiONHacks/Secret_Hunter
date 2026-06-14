package shop.sainionai.privacyguardian.scanner

import shop.sainionai.privacyguardian.model.DetectedTracker

/**
 * Curated tracker signature list (Exodus-style). Each signature is a class-path
 * prefix that appears literally in an app's DEX string pool when the SDK is present.
 *
 * This is a representative subset for the MVP. To go production-grade, replace this
 * with the full Exodus Privacy signature set (εxodus is AGPL / data is open) loaded
 * from a bundled JSON asset, and version it like the permission taxonomy.
 */
object TrackerSignatures {

    const val DB_VERSION = "1.0.0-subset"

    /** signature prefix (slash form) -> (display name, category) */
    private val signatures: List<Triple<String, String, String>> = listOf(
        // Analytics
        Triple("com/google/firebase/analytics", "Google Firebase Analytics", "Analytics"),
        Triple("com/google/android/gms/analytics", "Google Analytics", "Analytics"),
        Triple("com/flurry/android", "Flurry", "Analytics"),
        Triple("com/mixpanel/android", "Mixpanel", "Analytics"),
        Triple("com/amplitude/api", "Amplitude", "Analytics"),
        Triple("com/segment/analytics", "Segment", "Analytics"),
        Triple("com/yandex/metrica", "Yandex AppMetrica", "Analytics"),
        // Advertising / attribution
        Triple("com/google/android/gms/ads", "Google AdMob", "Advertising"),
        Triple("com/facebook/ads", "Facebook Audience Network", "Advertising"),
        Triple("com/appsflyer", "AppsFlyer", "Attribution"),
        Triple("com/adjust/sdk", "Adjust", "Attribution"),
        Triple("com/unity3d/ads", "Unity Ads", "Advertising"),
        Triple("com/applovin", "AppLovin", "Advertising"),
        Triple("com/ironsource", "ironSource", "Advertising"),
        Triple("io/branch/referral", "Branch", "Attribution"),
        // Social SDKs (identity / tracking surface)
        Triple("com/facebook/login", "Facebook Login", "Identification"),
        Triple("com/facebook/internal", "Facebook SDK", "Analytics"),
        // Crash / diagnostics
        Triple("com/google/firebase/crashlytics", "Firebase Crashlytics", "Crash reporting"),
        Triple("io/sentry", "Sentry", "Crash reporting"),
        Triple("com/bugsnag/android", "Bugsnag", "Crash reporting"),
        // Push / engagement
        Triple("com/onesignal", "OneSignal", "Push"),
        Triple("com/clevertap/android", "CleverTap", "Analytics"),
        Triple("com/moengage", "MoEngage", "Analytics")
    )

    /** Byte-search needles (the prefix encoded as it appears in the DEX string pool). */
    val needles: List<ByteArray> = signatures.map { it.first.toByteArray(Charsets.UTF_8) }

    fun trackerFor(index: Int): DetectedTracker {
        val (sig, name, cat) = signatures[index]
        return DetectedTracker(name = name, category = cat, signature = sig)
    }

    val size: Int get() = signatures.size
}
