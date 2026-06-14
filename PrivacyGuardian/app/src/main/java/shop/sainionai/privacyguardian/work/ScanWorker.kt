package shop.sainionai.privacyguardian.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import shop.sainionai.privacyguardian.data.ScanDiffer
import shop.sainionai.privacyguardian.data.ScanStore
import shop.sainionai.privacyguardian.scanner.ScannerEngine
import java.util.concurrent.TimeUnit

/**
 * Periodic background re-scan. Turns the app from a manual scanner into a monitor:
 * it scans, diffs against the previous scan, and notifies on notable changes (a new
 * high-risk app, a risk increase, or an app newly granted a sensitive permission).
 * All work stays on device.
 */
class ScanWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = runCatching {
        val ctx = applicationContext
        val store = ScanStore(ctx)
        val previous = store.loadLatest()                 // baseline before this run
        val apps = ScannerEngine(ctx).scanInstalledApps(includeSystem = false)
        val ts = System.currentTimeMillis()
        store.save(apps, ts)

        val diff = ScanDiffer.diff(previous, apps, null, ts)
        val notable = diff.notable
        if (notable.isNotEmpty()) {
            val top = notable.first()
            val title = "Privacy change detected"
            val body = buildString {
                append("${top.label}: ${top.detail}")
                if (notable.size > 1) append("  (+${notable.size - 1} more)")
            }
            Notifications.show(ctx, 1001, title, body)
        }
        Result.success()
    }.getOrElse { Result.retry() }

    companion object {
        private const val WORK_NAME = "privacy_guardian_periodic_scan"

        /** Schedule a daily background scan (kept >15 min per WorkManager minimum). */
        fun schedule(context: Context) {
            val req = PeriodicWorkRequestBuilder<ScanWorker>(24, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, req
            )
        }
    }
}
