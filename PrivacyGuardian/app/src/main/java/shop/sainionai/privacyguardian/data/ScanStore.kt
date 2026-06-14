package shop.sainionai.privacyguardian.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import shop.sainionai.privacyguardian.data.crypto.CryptoStore
import shop.sainionai.privacyguardian.model.DetectedTracker
import shop.sainionai.privacyguardian.model.PermissionCategory
import shop.sainionai.privacyguardian.model.RiskComponent
import shop.sainionai.privacyguardian.model.RiskLevel
import shop.sainionai.privacyguardian.model.RiskScore
import shop.sainionai.privacyguardian.model.ScannedApp
import shop.sainionai.privacyguardian.model.ScannedPermission
import java.io.File

/**
 * Persists scan results to internal storage, AES-256 encrypted at rest.
 * Files live in filesDir/scans/<timestamp>.pgs — app-private, never world-readable,
 * and the plaintext only exists in memory. Nothing is uploaded.
 */
class ScanStore(context: Context) {

    private val dir = File(context.filesDir, "scans").apply { mkdirs() }

    data class HistoryEntry(val timestamp: Long, val file: File)

    fun save(apps: List<ScannedApp>, timestamp: Long = System.currentTimeMillis()) {
        val json = serialize(apps, timestamp).toString().toByteArray(Charsets.UTF_8)
        File(dir, "$timestamp.pgs").writeBytes(CryptoStore.encrypt(json))
    }

    fun history(): List<HistoryEntry> =
        dir.listFiles { f -> f.extension == "pgs" }
            ?.mapNotNull { f -> f.nameWithoutExtension.toLongOrNull()?.let { HistoryEntry(it, f) } }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()

    fun loadLatest(): List<ScannedApp>? = history().firstOrNull()?.let { load(it.file) }

    /** Returns (current, previous) scans with timestamps for diffing, when available. */
    fun loadLatestTwo(): Pair<HistoryEntry, HistoryEntry?>? {
        val h = history()
        if (h.isEmpty()) return null
        return h[0] to h.getOrNull(1)
    }

    fun load(file: File): List<ScannedApp>? = runCatching {
        val plain = CryptoStore.decrypt(file.readBytes()).toString(Charsets.UTF_8)
        deserialize(JSONObject(plain))
    }.getOrNull()

    fun clear() { dir.listFiles()?.forEach { it.delete() } }

    // --- serialization (org.json; no extra deps) ---

    private fun serialize(apps: List<ScannedApp>, ts: Long): JSONObject {
        val root = JSONObject()
        root.put("schema", "1.1.0")
        root.put("timestamp", ts)
        val arr = JSONArray()
        for (a in apps) {
            val o = JSONObject()
            o.put("package", a.packageName)
            o.put("label", a.appLabel)
            o.put("version", a.versionName ?: JSONObject.NULL)
            o.put("system", a.isSystemApp)
            o.put("installer", a.installerPackage ?: JSONObject.NULL)

            val perms = JSONArray()
            a.permissions.forEach { p ->
                perms.put(JSONObject().apply {
                    put("name", p.androidName); put("granted", p.granted)
                    put("category", p.category.name); put("points", p.basePoints)
                    put("expected", p.expected)
                })
            }
            o.put("permissions", perms)

            val trk = JSONArray()
            a.trackers.forEach { t ->
                trk.put(JSONObject().apply {
                    put("name", t.name); put("category", t.category); put("signature", t.signature)
                })
            }
            o.put("trackers", trk)
            o.put("risk", riskToJson(a.risk))
            arr.put(o)
        }
        root.put("apps", arr)
        return root
    }

    private fun riskToJson(r: RiskScore): JSONObject {
        val comps = JSONArray()
        r.components.forEach { c ->
            comps.put(JSONObject().apply {
                put("name", c.name); put("value", c.value); put("weight", c.weight)
                put("assessed", c.assessed); put("rationale", c.rationale)
            })
        }
        return JSONObject().apply {
            put("overall", r.overall); put("level", r.level.name)
            put("confidence", r.confidence); put("engine", r.engineVersion)
            put("components", comps)
        }
    }

    private fun deserialize(root: JSONObject): List<ScannedApp> {
        val out = mutableListOf<ScannedApp>()
        val arr = root.getJSONArray("apps")
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val perms = o.getJSONArray("permissions").let { pa ->
                (0 until pa.length()).map { j ->
                    val p = pa.getJSONObject(j)
                    ScannedPermission(
                        androidName = p.getString("name"),
                        granted = p.getBoolean("granted"),
                        category = PermissionCategory.valueOf(p.getString("category")),
                        basePoints = p.getInt("points"),
                        expected = p.optBoolean("expected", true)
                    )
                }
            }
            val trk = o.getJSONArray("trackers").let { ta ->
                (0 until ta.length()).map { j ->
                    val t = ta.getJSONObject(j)
                    DetectedTracker(t.getString("name"), t.getString("category"), t.getString("signature"))
                }
            }
            out.add(
                ScannedApp(
                    packageName = o.getString("package"),
                    appLabel = o.getString("label"),
                    versionName = o.optString("version", null),
                    isSystemApp = o.getBoolean("system"),
                    installerPackage = o.optString("installer", null),
                    permissions = perms,
                    trackers = trk,
                    risk = riskFromJson(o.getJSONObject("risk"))
                )
            )
        }
        return out
    }

    private fun riskFromJson(o: JSONObject): RiskScore {
        val ca = o.getJSONArray("components")
        val comps = (0 until ca.length()).map { i ->
            val c = ca.getJSONObject(i)
            RiskComponent(
                c.getString("name"), c.getInt("value"), c.getDouble("weight"),
                c.getBoolean("assessed"), c.getString("rationale")
            )
        }
        return RiskScore(
            overall = o.getInt("overall"),
            level = RiskLevel.valueOf(o.getString("level")),
            confidence = o.getDouble("confidence"),
            components = comps,
            engineVersion = o.getString("engine")
        )
    }
}
