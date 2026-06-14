package shop.sainionai.privacyguardian.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Risk palette — referenced directly by the UI for level chips.
val RiskLow = Color(0xFF2E7D32)
val RiskMedium = Color(0xFFF9A825)
val RiskHigh = Color(0xFFEF6C00)
val RiskCritical = Color(0xFFC62828)

private val Light = lightColorScheme(
    primary = Color(0xFF1B5E20),
    secondary = Color(0xFF00695C)
)
private val Dark = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFF4DB6AC)
)

@Composable
fun PrivacyGuardianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) Dark else Light,
        content = content
    )
}
