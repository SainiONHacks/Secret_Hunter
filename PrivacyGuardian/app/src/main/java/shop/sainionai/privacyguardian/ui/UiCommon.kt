package shop.sainionai.privacyguardian.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import shop.sainionai.privacyguardian.model.RiskLevel
import shop.sainionai.privacyguardian.ui.theme.RiskCritical
import shop.sainionai.privacyguardian.ui.theme.RiskHigh
import shop.sainionai.privacyguardian.ui.theme.RiskLow
import shop.sainionai.privacyguardian.ui.theme.RiskMedium

fun colorFor(level: RiskLevel): Color = when (level) {
    RiskLevel.LOW -> RiskLow
    RiskLevel.MEDIUM -> RiskMedium
    RiskLevel.HIGH -> RiskHigh
    RiskLevel.CRITICAL -> RiskCritical
}

@Composable
fun RiskChip(level: RiskLevel, modifier: Modifier = Modifier) {
    Text(
        text = level.label.uppercase(),
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(colorFor(level), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}
