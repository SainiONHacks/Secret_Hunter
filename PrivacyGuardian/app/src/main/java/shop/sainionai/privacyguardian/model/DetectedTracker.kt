package shop.sainionai.privacyguardian.model

/**
 * A third-party tracker / analytics / ad SDK found embedded in an app.
 * Detection is signature-based (see TrackerDetector) — same approach Exodus uses.
 */
data class DetectedTracker(
    val name: String,
    val category: String,       // e.g. "Analytics", "Advertising", "Crash reporting"
    val signature: String       // the class-path prefix that matched
)
