package shop.sainionai.privacyguardian.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** First-run flag persisted in SharedPreferences. */
object OnboardingPrefs {
    private const val PREFS = "pg_prefs"
    private const val KEY_DONE = "onboarded"
    fun isDone(c: Context) =
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_DONE, false)
    fun setDone(c: Context) =
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_DONE, true).apply()
}

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Privacy Guardian", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Section("Why this app monitors",
            "It analyses the permissions, trackers and (optionally) the network destinations of " +
            "your installed apps, so you can see which ones have unusual privacy exposure.")
        Section("What it processes",
            "App names, permissions and embedded SDK signatures — all on your device. " +
            "It never reads your contacts, messages, photos or files.")
        Section("What never leaves your phone",
            "Everything stays local and is encrypted at rest. Reports are saved on-device and " +
            "shared only if you choose to.")
        Section("What the scores mean",
            "Scores measure privacy EXPOSURE, not proof of wrongdoing. A high score means an app " +
            "could access a lot — investigate, don't assume.")

        Spacer(Modifier.height(8.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("I understand, continue")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Section(title: String, body: String) {
    Column {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
