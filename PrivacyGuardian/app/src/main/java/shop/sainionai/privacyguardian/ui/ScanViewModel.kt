package shop.sainionai.privacyguardian.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import shop.sainionai.privacyguardian.model.ScannedApp
import shop.sainionai.privacyguardian.model.ScanDiff
import shop.sainionai.privacyguardian.data.ScanStore
import shop.sainionai.privacyguardian.data.ScanDiffer
import shop.sainionai.privacyguardian.scanner.ScannerEngine

data class ScanUiState(
    val isScanning: Boolean = false,
    val apps: List<ScannedApp> = emptyList(),
    val lastScanMillis: Long? = null,
    val diff: ScanDiff? = null,
    val visibilityRestricted: Boolean = false,
    val error: String? = null
) {
    val critical get() = apps.count { it.risk.level.name == "CRITICAL" }
    val high     get() = apps.count { it.risk.level.name == "HIGH" }
    val medium   get() = apps.count { it.risk.level.name == "MEDIUM" }
    val low      get() = apps.count { it.risk.level.name == "LOW" }

    /** Simple inverse-of-risk privacy score for the dashboard headline. */
    val privacyScore: Int
        get() {
            if (apps.isEmpty()) return 100
            val avg = apps.map { it.risk.overall }.average()
            return (100 - avg).coerceIn(0.0, 100.0).toInt()
        }
}

class ScanViewModel(app: Application) : AndroidViewModel(app) {

    private val engine = ScannerEngine(app.applicationContext)
    private val store = ScanStore(app.applicationContext)

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    init {
        // Restore the most recent scan, and diff the two latest if present.
        viewModelScope.launch {
            val pair = runCatching { store.loadLatestTwo() }.getOrNull()
            if (pair != null) {
                val current = store.load(pair.first.file)
                if (!current.isNullOrEmpty()) {
                    val prev = pair.second?.let { store.load(it.file) }
                    val diff = ScanDiffer.diff(prev, current, pair.second?.timestamp, pair.first.timestamp)
                    _state.value = ScanUiState(
                        apps = current, lastScanMillis = pair.first.timestamp, diff = diff
                    )
                }
            }
        }
    }

    fun scan(includeSystem: Boolean = false) {
        if (_state.value.isScanning) return
        _state.value = _state.value.copy(isScanning = true, error = null)
        viewModelScope.launch {
            val previous = _state.value.apps.takeIf { it.isNotEmpty() }
            val prevTs = _state.value.lastScanMillis
            runCatching { engine.scanInstalledApps(includeSystem) }
                .onSuccess { apps ->
                    val ts = System.currentTimeMillis()
                    runCatching { store.save(apps, ts) }
                    val restricted = runCatching { engine.visibilityLooksRestricted() }
                        .getOrDefault(false)
                    _state.value = ScanUiState(
                        isScanning = false,
                        apps = apps,
                        lastScanMillis = ts,
                        diff = ScanDiffer.diff(previous, apps, prevTs, ts),
                        visibilityRestricted = restricted
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isScanning = false,
                        error = e.message ?: "Scan failed"
                    )
                }
        }
    }

    fun appByPackage(pkg: String): ScannedApp? =
        _state.value.apps.firstOrNull { it.packageName == pkg }
}
