package shop.sainionai.privacyguardian.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import shop.sainionai.privacyguardian.canary.CanaryManager
import shop.sainionai.privacyguardian.canary.CanaryWatcher
import shop.sainionai.privacyguardian.evidence.EvidenceRecorder
import shop.sainionai.privacyguardian.model.Canary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanaryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val manager = remember { CanaryManager(context) }
    val watcher = remember { CanaryWatcher(EvidenceRecorder.get(context)) }
    val canaries = remember { mutableStateListOf<Canary>() }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Canary evidence") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            Text(
                "Plant decoy resources. If something reads them, it's logged to the evidence " +
                        "timeline. Without root we detect the access, not which app — pair it with " +
                        "the network timeline to build a case.",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val c = manager.createFileCanary()
                    watcher.watch(c.locationHint)
                    canaries.add(c)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Plant file canary (DO_NOT_SHARE.txt)") }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val c = manager.createImageCanary()
                    watcher.watch(c.locationHint)
                    canaries.add(c)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Plant image canary (privacy_test.jpg)") }

            Spacer(Modifier.height(16.dp))
            Text("Planted (${canaries.size})", fontWeight = FontWeight.Bold)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(canaries) { c ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(c.displayName, fontWeight = FontWeight.Medium)
                            Text(c.type.name, fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
