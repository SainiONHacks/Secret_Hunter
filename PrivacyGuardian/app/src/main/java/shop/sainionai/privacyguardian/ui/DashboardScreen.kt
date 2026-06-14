package shop.sainionai.privacyguardian.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shop.sainionai.privacyguardian.model.ScannedApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    vm: ScanViewModel,
    onAppClick: (String) -> Unit,
    onExport: () -> Unit,
    onOpenMonitor: () -> Unit,
    onOpenCanary: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Privacy Guardian") }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Privacy score", fontSize = 13.sp)
                    Text(
                        if (state.apps.isEmpty()) "--" else "${state.privacyScore}/100",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Counter("Critical", state.critical)
                        Counter("High", state.high)
                        Counter("Medium", state.medium)
                        Counter("Low", state.low)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { vm.scan() }, enabled = !state.isScanning) {
                    Text(if (state.isScanning) "Scanning…" else "Scan apps")
                }
                if (state.apps.isNotEmpty()) {
                    OutlinedButton(onClick = onExport) { Text("Export") }
                }
                if (shop.sainionai.privacyguardian.BuildConfig.FULL_FEATURES) {
                    OutlinedButton(onClick = onOpenMonitor) { Text("Network") }
                }
                OutlinedButton(onClick = onOpenCanary) { Text("Canary") }
            }

            state.diff?.takeIf { !it.isEmpty }?.let { diff ->
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Since last scan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        diff.changes.take(6).forEach { c ->
                            Text("• ${c.label}: ${c.detail}", fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp))
                        }
                        if (diff.changes.size > 6) {
                            Text("+${diff.changes.size - 6} more", fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text("Error: $it", color = MaterialTheme.colorScheme.error)
            }

            if (state.visibilityRestricted) {
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        "Only a few apps are visible to scan. This device is limiting app " +
                            "visibility — grant the app-list permission (or install from a build " +
                            "with the security-app declaration) to scan everything installed.",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (state.isScanning && state.apps.isEmpty()) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { CircularProgressIndicator() }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.apps, key = { it.packageName }) { app ->
                        AppRow(app) { onAppClick(app.packageName) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Counter(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 11.sp)
    }
}

@Composable
private fun AppRow(app: ScannedApp, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(app.appLabel, fontWeight = FontWeight.SemiBold)
                Text(
                    app.packageName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${app.grantedSensitive.size} sensitive permission(s) granted",
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${app.risk.overall}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                RiskChip(app.risk.level)
            }
        }
    }
}
