package shop.sainionai.privacyguardian.data

import org.json.JSONArray
import org.json.JSONObject
import shop.sainionai.privacyguardian.model.ScannedApp
import shop.sainionai.privacyguardian.risk.RiskEngine
import shop.sainionai.privacyguardian.scanner.PermissionClassifier

/**
 * Phase 1 reporting: build a self-contained, on-device JSON report.
 *
 * Stays true to the mission — the report carries only locally-derived metadata
 * (package names, permissions, scores). No contacts, no media, no SMS content.
 * The engine + taxonomy versions are embedded so any score stays explainable later.
 */
object ReportGenerator {

    fun buildJson(apps: List<ScannedApp>, generatedAtMillis: Long): String {
        val root = JSONObject()
        root.put("report_type", "privacy_guardian_scan")
        root.put("schema_version", "1.0.0")
        root.put("engine_version", RiskEngine.ENGINE_VERSION)
        root.put("taxonomy_version", PermissionClassifier.TAXONOMY_VERSION)
        root.put("generated_at", generatedAtMillis)
        root.put("app_count", apps.size)

        val summary = JSONObject()
        summary.put("critical", apps.count { it.risk.level.name == "CRITICAL" })
        summary.put("high", apps.count { it.risk.level.name == "HIGH" })
        summary.put("medium", apps.count { it.risk.level.name == "MEDIUM" })
        summary.put("low", apps.count { it.risk.level.name == "LOW" })
        root.put("summary", summary)

        val arr = JSONArray()
        for (app in apps) {
            val a = JSONObject()
            a.put("package", app.packageName)
            a.put("label", app.appLabel)
            a.put("version", app.versionName ?: "")
            a.put("installer", app.installerPackage ?: "")
            a.put("system_app", app.isSystemApp)
            a.put("risk_overall", app.risk.overall)
            a.put("risk_level", app.risk.level.name)
            a.put("risk_confidence", app.risk.confidence)

            val perms = JSONArray()
            app.grantedSensitive.forEach { p ->
                val pj = JSONObject()
                pj.put("permission", p.androidName)
                pj.put("category", p.category.label)
                pj.put("base_points", p.basePoints)
                perms.put(pj)
            }
            a.put("granted_sensitive_permissions", perms)

            val trackers = JSONArray()
            app.trackers.forEach { t ->
                trackers.put(JSONObject().apply {
                    put("name", t.name); put("category", t.category)
                })
            }
            a.put("trackers", trackers)
            arr.put(a)
        }
        root.put("apps", arr)
        return root.toString(2)
    }
}
