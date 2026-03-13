package com.bedrockexecutor.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Color Palette — dark executor aesthetic with green accent
val Background = Color(0xFF0A0A0F)
val Surface = Color(0xFF111118)
val SurfaceVariant = Color(0xFF1A1A24)
val CardColor = Color(0xFF16161F)
val AccentGreen = Color(0xFF00FF88)
val AccentGreenDim = Color(0xFF00CC6A)
val AccentBlue = Color(0xFF4DABFF)
val TextPrimary = Color(0xFFE8E8F0)
val TextSecondary = Color(0xFF8888A0)
val TextMuted = Color(0xFF55556A)
val BorderColor = Color(0xFF2A2A3A)
val ErrorRed = Color(0xFFFF4466)
val WarningYellow = Color(0xFFFFCC00)
val ConsoleGreen = Color(0xFF00FF88)
val ConsoleBg = Color(0xFF080810)

private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Color(0xFF001A0D),
    primaryContainer = Color(0xFF003319),
    onPrimaryContainer = AccentGreen,
    secondary = AccentBlue,
    onSecondary = Color(0xFF001A33),
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    error = ErrorRed,
    onError = Color.White,
)

@Composable
fun BedrockExecutorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(
            headlineLarge = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            ),
            headlineMedium = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary,
                letterSpacing = (-0.3).sp
            ),
            titleLarge = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextPrimary
            ),
            titleMedium = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = TextPrimary
            ),
            bodyLarge = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = TextPrimary
            ),
            bodyMedium = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = TextSecondary
            ),
            labelSmall = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
        ),
        content = content
    )
}
