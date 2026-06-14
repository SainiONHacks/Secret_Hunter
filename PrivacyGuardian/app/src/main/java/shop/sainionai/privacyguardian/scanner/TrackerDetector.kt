package shop.sainionai.privacyguardian.scanner

import android.content.pm.PackageManager
import shop.sainionai.privacyguardian.model.DetectedTracker
import java.io.File
import java.util.zip.ZipFile

/**
 * Static tracker detection.
 *
 * Technique (same as Exodus Privacy's static analyser): an installed app's APK is
 * readable at applicationInfo.sourceDir. Inside it, every Dalvik class name is stored
 * literally in the DEX string pool — so a tracker SDK like Firebase Analytics leaves
 * the bytes "com/google/firebase/analytics" in classes*.dex. We scan those bytes for
 * known signature prefixes. No code is executed; nothing leaves the device.
 *
 * Limits: catches SDKs present in plain DEX. Heavily obfuscated/renamed or
 * reflection-loaded SDKs can evade this — documented, not hidden.
 */
class TrackerDetector(private val pm: PackageManager) {

    fun detect(packageName: String): List<DetectedTracker> {
        val sourceDir = runCatching {
            pm.getApplicationInfo(packageName, 0).sourceDir
        }.getOrNull() ?: return emptyList()

        val found = sortedSetOf<Int>()
        val maxNeedle = TrackerSignatures.needles.maxOf { it.size }
        runCatching {
            ZipFile(File(sourceDir)).use { zip ->
                val dexEntries = zip.entries().asSequence()
                    .filter { it.name.endsWith(".dex") }
                    .toList()
                for (entry in dexEntries) {
                    scanStream(zip.getInputStream(entry), maxNeedle, found)
                    if (found.size == TrackerSignatures.size) break
                }
            }
        }
        return found.map { TrackerSignatures.trackerFor(it) }
    }

    /**
     * Stream the DEX in fixed chunks instead of loading it whole (large APKs can have
     * 50 MB+ DEX → OOM). A carry buffer of (maxNeedle-1) bytes is prepended to each
     * chunk so a signature straddling a chunk boundary is still matched.
     */
    private fun scanStream(input: java.io.InputStream, maxNeedle: Int, found: MutableSet<Int>) {
        input.buffered().use { stream ->
            val chunk = 64 * 1024
            val carryLen = (maxNeedle - 1).coerceAtLeast(0)
            var carry = ByteArray(0)
            val buf = ByteArray(chunk)
            while (true) {
                val n = stream.read(buf)
                if (n <= 0) break
                val block = ByteArray(carry.size + n)
                System.arraycopy(carry, 0, block, 0, carry.size)
                System.arraycopy(buf, 0, block, carry.size, n)
                TrackerSignatures.needles.forEachIndexed { i, needle ->
                    if (i !in found && contains(block, needle)) found.add(i)
                }
                if (found.size == TrackerSignatures.size) return
                carry = if (carryLen > 0 && block.size >= carryLen)
                    block.copyOfRange(block.size - carryLen, block.size) else ByteArray(0)
            }
        }
    }

    /** Naive byte-substring search; fine for a handful of needles on a background thread. */
    private fun contains(haystack: ByteArray, needle: ByteArray): Boolean {
        if (needle.isEmpty() || haystack.size < needle.size) return false
        val first = needle[0]
        val last = haystack.size - needle.size
        var i = 0
        while (i <= last) {
            if (haystack[i] == first) {
                var j = 1
                while (j < needle.size && haystack[i + j] == needle[j]) j++
                if (j == needle.size) return true
            }
            i++
        }
        return false
    }
}
