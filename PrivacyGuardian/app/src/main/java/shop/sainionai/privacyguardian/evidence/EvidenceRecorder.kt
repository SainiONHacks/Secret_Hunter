package shop.sainionai.privacyguardian.evidence

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import shop.sainionai.privacyguardian.data.crypto.CryptoStore
import shop.sainionai.privacyguardian.model.EvidenceEvent
import shop.sainionai.privacyguardian.model.EvidenceType
import java.io.File

/**
 * Append-only evidence log, AES-256 encrypted at rest (Phase 3).
 * Single process-wide recorder so the VPN service, canary observers and scanner can
 * all feed the same timeline. Events are also exposed as a StateFlow for live UI.
 */
class EvidenceRecorder private constructor(private val file: File) {

    private val _events = MutableStateFlow<List<EvidenceEvent>>(emptyList())
    val events: StateFlow<List<EvidenceEvent>> = _events

    init { _events.value = load() }

    @Synchronized
    fun record(packageName: String, type: EvidenceType, detail: String) {
        val e = EvidenceEvent(System.currentTimeMillis(), packageName, type, detail)
        val updated = _events.value + e
        _events.value = updated
        persist(updated)
    }

    fun forApp(packageName: String): List<EvidenceEvent> =
        _events.value.filter { it.packageName == packageName }.sortedBy { it.timestamp }

    /**
     * Lightweight correlation: flag apps where a sensitive-permission event is
     * followed by an upload within [windowMs]. Suggestive, never asserted as proof.
     */
    fun correlatedUploads(packageName: String, windowMs: Long = 120_000): Int {
        val ev = forApp(packageName)
        var hits = 0
        for (i in ev.indices) {
            if (ev[i].type == EvidenceType.PERMISSION_ACCESSED) {
                val end = ev[i].timestamp + windowMs
                if (ev.drop(i + 1).any {
                        it.type == EvidenceType.UPLOAD_DETECTED && it.timestamp <= end
                    }) hits++
            }
        }
        return hits
    }

    fun clear() { _events.value = emptyList(); runCatching { file.delete() } }

    private fun persist(events: List<EvidenceEvent>) = runCatching {
        val arr = JSONArray()
        events.takeLast(MAX_EVENTS).forEach { e ->
            arr.put(JSONObject().apply {
                put("t", e.timestamp); put("pkg", e.packageName)
                put("type", e.type.name); put("detail", e.detail)
            })
        }
        file.writeBytes(CryptoStore.encrypt(arr.toString().toByteArray()))
    }

    private fun load(): List<EvidenceEvent> = runCatching {
        if (!file.exists()) return emptyList()
        val arr = JSONArray(CryptoStore.decrypt(file.readBytes()).toString(Charsets.UTF_8))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            EvidenceEvent(o.getLong("t"), o.getString("pkg"),
                EvidenceType.valueOf(o.getString("type")), o.getString("detail"))
        }
    }.getOrDefault(emptyList())

    companion object {
        private const val MAX_EVENTS = 5000
        @Volatile private var instance: EvidenceRecorder? = null

        fun get(context: Context): EvidenceRecorder =
            instance ?: synchronized(this) {
                instance ?: EvidenceRecorder(
                    File(context.applicationContext.filesDir, "evidence.pgs")
                ).also { instance = it }
            }
    }
}
