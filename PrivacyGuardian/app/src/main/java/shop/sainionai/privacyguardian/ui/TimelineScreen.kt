package shop.sainionai.privacyguardian.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import shop.sainionai.privacyguardian.model.EvidenceEvent
import shop.sainionai.privacyguardian.model.EvidenceType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(appLabel: String, events: List<EvidenceEvent>, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Evidence timeline") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            Text(appLabel, fontWeight = FontWeight.SemiBold)
            Text(
                "Correlated events, not proof of misuse. Causal proof needs the V2 research build.",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            if (events.isEmpty()) {
                Text("No events recorded yet.", fontSize = 13.sp)
            } else {
                LazyColumn { items(events) { TimelineRow(it) } }
            }
        }
    }
}

@Composable
private fun TimelineRow(e: EvidenceEvent) {
    val time = remember(e.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(e.timestamp))
    }
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(11.dp).clip(CircleShape).background(dotColor(e.type)))
            Box(Modifier.width(1.5.dp).height(36.dp)
                .background(MaterialTheme.colorScheme.outlineVariant))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.padding(bottom = 8.dp)) {
            Text(time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(e.type.label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (e.detail.isNotBlank())
                Text(e.detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun dotColor(type: EvidenceType): Color = when (type.severity) {
    1 -> Color(0xFF5F5E5A)
    2 -> Color(0xFFEF6C00)
    3 -> Color(0xFFC62828)
    else -> Color(0xFFC62828)
}
