package shop.sainionai.privacyguardian

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import shop.sainionai.privacyguardian.data.Exporters
import shop.sainionai.privacyguardian.data.ReportGenerator
import shop.sainionai.privacyguardian.evidence.EvidenceRecorder
import shop.sainionai.privacyguardian.network.PrivacyVpnService
import shop.sainionai.privacyguardian.ui.AppDetailScreen
import shop.sainionai.privacyguardian.ui.DashboardScreen
import shop.sainionai.privacyguardian.ui.NetworkMonitorScreen
import shop.sainionai.privacyguardian.ui.OnboardingPrefs
import shop.sainionai.privacyguardian.ui.OnboardingScreen
import shop.sainionai.privacyguardian.ui.ScanViewModel
import shop.sainionai.privacyguardian.ui.TimelineScreen
import shop.sainionai.privacyguardian.ui.theme.PrivacyGuardianTheme
import shop.sainionai.privacyguardian.work.Notifications
import shop.sainionai.privacyguardian.work.ScanWorker
import java.io.File

class MainActivity : ComponentActivity() {

    private val vm: ScanViewModel by viewModels()

    private val vpnConsent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startService(Intent(this, PrivacyVpnService::class.java))
            Toast.makeText(this, "Monitoring started (skeleton)", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "VPN permission declined", Toast.LENGTH_SHORT).show()
        }
    }

    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* best-effort; alerts simply won't show if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Notifications.ensureChannel(this)
        ScanWorker.schedule(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            PrivacyGuardianTheme {
                App(
                    vm = vm,
                    onExport = ::exportReports,
                    onStartMonitoring = ::startMonitoring,
                    onboarded = OnboardingPrefs.isDone(this),
                    onOnboardingDone = { OnboardingPrefs.setDone(this) }
                )
            }
        }
    }

    private fun startMonitoring() {
        if (!BuildConfig.FULL_FEATURES) return   // network monitor not in the Play build
        val consent = PrivacyVpnService.consentIntent(this)
        if (consent != null) vpnConsent.launch(consent) else
            startService(Intent(this, PrivacyVpnService::class.java))
    }

    /** Writes JSON + CSV + PDF locally and shares the PDF via a FileProvider URI. */
    private fun exportReports() {
        val apps = vm.state.value.apps
        if (apps.isEmpty()) return
        val dir = File(cacheDir, "reports").apply { mkdirs() }
        val ts = System.currentTimeMillis()

        File(dir, "report_$ts.json").writeText(ReportGenerator.buildJson(apps, ts))
        File(dir, "report_$ts.csv").writeText(Exporters.csv(apps))
        val pdf = Exporters.pdf(apps, File(dir, "report_$ts.pdf"))

        val uri = androidx.core.content.FileProvider.getUriForFile(
            this, "$packageName.fileprovider", pdf
        )
        Toast.makeText(this, "Saved JSON, CSV, PDF in app storage", Toast.LENGTH_LONG).show()
        runCatching {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "Share report"))
        }
    }
}

@Composable
private fun App(
    vm: ScanViewModel,
    onExport: () -> Unit,
    onStartMonitoring: () -> Unit,
    onboarded: Boolean,
    onOnboardingDone: () -> Unit
) {
    val nav = rememberNavController()
    val start = if (onboarded) "dashboard" else "onboarding"
    NavHost(navController = nav, startDestination = start) {
        composable("onboarding") {
            OnboardingScreen(onContinue = {
                onOnboardingDone()
                nav.navigate("dashboard") { popUpTo("onboarding") { inclusive = true } }
            })
        }
        composable("dashboard") {
            DashboardScreen(
                vm = vm,
                onAppClick = { pkg -> nav.navigate("detail/$pkg") },
                onExport = onExport,
                onOpenMonitor = { nav.navigate("network") },
                onOpenCanary = { nav.navigate("canary") }
            )
        }
        composable("detail/{pkg}") { entry ->
            val pkg = entry.arguments?.getString("pkg").orEmpty()
            AppDetailScreen(
                app = vm.appByPackage(pkg),
                onBack = { nav.popBackStack() },
                onViewTimeline = { nav.navigate("timeline/$it") }
            )
        }
        composable("timeline/{pkg}") { entry ->
            val pkg = entry.arguments?.getString("pkg").orEmpty()
            val app = vm.appByPackage(pkg)
            val recorder = EvidenceRecorder.get(androidx.compose.ui.platform.LocalContext.current)
            TimelineScreen(
                appLabel = app?.appLabel ?: pkg,
                events = recorder.forApp(pkg),
                onBack = { nav.popBackStack() }
            )
        }
        composable("network") {
            NetworkMonitorScreen(
                onBack = { nav.popBackStack() },
                onStartMonitoring = onStartMonitoring
            )
        }
        composable("canary") {
            shop.sainionai.privacyguardian.ui.CanaryScreen(onBack = { nav.popBackStack() })
        }
    }
}
