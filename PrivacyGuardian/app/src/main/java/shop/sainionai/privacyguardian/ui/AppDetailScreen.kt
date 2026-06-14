package shop.sainionai.privacyguardian.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import shop.sainionai.privacyguardian.model.ScannedApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(app: ScannedApp?, onBack: () -> Unit, onViewTimeline: (String) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(app?.appLabel ?: "App") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }) { pad ->
        if (app == null) {
            Column(Modifier.fillMaxSize().padding(pad), Arrangement.Center, Alignment.CenterHorizontally) {
                Text("App not found. Re-run the scan.")
            }
            return@Scaffold
        }

        Column(
            Modifier.fillMaxSize().padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Risk ${app.risk.overall}/100", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        RiskChip(app.risk.level)
                    }
                    Text(
                        "Confidence ${(app.risk.confidence * 100).toInt()}%  •  engine v${app.risk.engineVersion}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(app.packageName, fontSize = 12.sp)
                    app.versionName?.let { Text("Version $it", fontSize = 12.sp) }
                    app.installerPackage?.let { Text("Installer: $it", fontSize = 12.sp) }
                }
            }

            androidx.compose.material3.OutlinedButton(
                onClick = { onViewTimeline(app.packageName) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("View evidence timeline") }

            Text("Score breakdown", fontWeight = FontWeight.Bold)
            app.risk.components.forEach { c ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row {
                            Text(c.name, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Text(
                                if (c.assessed) "${c.value}  (w=${c.weight})" else "not assessed",
                                color = if (c.assessed) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(c.rationale, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Text("Granted sensitive permissions", fontWeight = FontWeight.Bold)
            if (app.grantedSensitive.isEmpty()) {
                Text("None currently granted.", fontSize = 13.sp)
            } else {
                app.grantedSensitive.forEach { p ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(p.shortName, fontWeight = FontWeight.Medium)
                                Text(p.category.label, fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("${p.basePoints}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text("Embedded trackers (${app.trackers.size})", fontWeight = FontWeight.Bold)
            if (app.trackers.isEmpty()) {
                Text("No known tracker SDKs detected.", fontSize = 13.sp)
            } else {
                app.trackers.forEach { t ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(t.name, fontWeight = FontWeight.Medium)
                                Text(t.category, fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Note: this reflects requested/granted permissions only. It is an " +
                        "indicator of privacy exposure, not proof of misuse. Network and " +
                        "runtime behaviour are assessed in later phases.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
