package shop.sainionai.privacyguardian.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shop.sainionai.privacyguardian.model.Destination
import shop.sainionai.privacyguardian.network.ConnectionTracker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkMonitorScreen(onBack: () -> Unit, onStartMonitoring: () -> Unit) {
    val destinations by ConnectionTracker.destinations.collectAsStateWithLifecycle()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Network monitor") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            Text(
                "Destinations are server locations (hosting provider), not where your " +
                        "data legally resides. Hostnames/volumes only — payloads are not inspected.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.material3.OutlinedButton(
                onClick = onStartMonitoring,
                modifier = Modifier.padding(top = 12.dp)
            ) { Text("Start monitoring (skeleton)") }
            if (destinations.isEmpty()) {
                Text(
                    "No traffic captured yet. Start monitoring to populate this list.",
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    items(destinations, key = { it.ip }) { DestinationRow(it) }
                }
            }
        }
    }
}

@Composable
private fun DestinationRow(d: Destination) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(d.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                val where = d.geo?.let { "${it.city}, ${it.countryCode} • ${it.org}" } ?: d.ip
                Text(where, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${d.connections} connection(s)", fontSize = 11.sp)
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text("${fmt(d.bytesOut)} ↑", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("${fmt(d.bytesIn)} ↓", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (d.geo?.isCdn == true) {
                    Text("CDN", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun fmt(bytes: Long): String = when {
    bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
    bytes >= 1_000 -> "%.1f KB".format(bytes / 1_000.0)
    else -> "$bytes B"
}
