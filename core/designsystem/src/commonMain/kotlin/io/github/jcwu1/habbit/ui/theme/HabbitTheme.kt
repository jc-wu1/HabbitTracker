package io.github.jcwu1.habbit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val habbitColors = lightColorScheme(
    primary = Color(0xFF514EE5),
    onPrimary = Color.White,
    surface = Color(0xFFF7F7FC),
    onSurface = Color(0xFF25263D),
    onSurfaceVariant = Color(0xFF6B708D),
    outlineVariant = Color(0xFFD2D4E7),
)

private val habbitTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1).sp,
    ),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 24.sp),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp),
    labelLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
    ),
)

@Composable
fun HabbitTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = habbitColors, typography = habbitTypography, content = content)
}
