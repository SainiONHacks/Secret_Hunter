package shop.sainionai.privacyguardian.canary

import android.os.Build
import android.os.FileObserver
import shop.sainionai.privacyguardian.evidence.EvidenceRecorder
import shop.sainionai.privacyguardian.model.EvidenceType
import java.io.File

/**
 * Watches canary files for access events and records them as evidence (Phase 4).
 *
 * Honest scope: FileObserver tells us a file was opened/read, NOT which app did it
 * (no root). So we record a CANARY_ACCESSED event attributed to "unknown" — a strong
 * signal that something read a resource it shouldn't, without claiming who. Pairing
 * this with the network timeline is what makes it persuasive.
 */
class CanaryWatcher(
    private val recorder: EvidenceRecorder
) {
    private val observers = mutableListOf<FileObserver>()

    fun watch(path: String) {
        val file = File(path)
        val obs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(file, ACCESS or OPEN) {
                override fun onEvent(event: Int, p: String?) = onAccess(file.name)
            }
        } else {
            @Suppress("DEPRECATION")
            object : FileObserver(path, ACCESS or OPEN) {
                override fun onEvent(event: Int, p: String?) = onAccess(file.name)
            }
        }
        obs.startWatching()
        observers.add(obs)
    }

    private fun onAccess(name: String) {
        recorder.record(
            packageName = "unknown",
            type = EvidenceType.CANARY_ACCESSED,
            detail = "Canary '$name' was read (accessing app not identifiable without root)"
        )
    }

    fun stop() { observers.forEach { it.stopWatching() }; observers.clear() }
}
