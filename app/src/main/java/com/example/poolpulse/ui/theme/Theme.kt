package com.example.poolpulse.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poolpulse.R

// Traditional Pool Blues & Warm Beach Palette
val PoolBg = Color(0xFF0B192C)         // Deep Coastal Navy
val PoolCard = Color(0xFF132742)       // Ocean Slate Card
val PoolAccent = Color(0xFF1C3557)     // Lagoon Surface Accent
val PoolCyan = Color(0xFF38BDF8)       // Classic Pool Aqua
val PoolBlue = Color(0xFF0284C7)       // Deep Pool Blue
val PoolSand = Color(0xFFF3D5A5)       // Warm Beach Sand
val PoolOrange = Color(0xFFF59E0B)     // Warm Sun Amber (Target)
val PoolGreen = Color(0xFF2DD4BF)      // Seafoam Teal (Balanced / OK)
val PoolRed = Color(0xFFF87171)        // Soft Coral Red (Alert)
val PoolPurple = Color(0xFF7DD3FC)     // Soft Sky Blue Accent
val PoolMagenta = Color(0xFF38BDF8)    // Mapped to Pool Aqua
val PoolLime = Color(0xFFF3D5A5)       // Mapped to Warm Beach Sand
val PoolYellow = Color(0xFFF3D5A5)     // Mapped to Warm Beach Sand
val PoolText = Color(0xFFF1F5F9)       // Crisp White-Blue
val PoolMuted = Color(0xFF94A3B8)      // Driftwood Gray-Blue
val PoolBorder = Color(0x24FFFFFF)     // Subtle Card Border
val PoolHeroStart = Color(0xFF153450)  // Coastal Lagoon Hero Tint
val PoolTrackBg = Color(0xFF081220)    // Dark Water Track

val JakartaFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans, FontWeight.ExtraBold)
)

// Keep alias so any reference compiles cleanly with neat sans-serif font
val BangersFontFamily = JakartaFontFamily

private val PoolDarkColorScheme = darkColorScheme(
    primary = PoolCyan,
    onPrimary = PoolBg,
    secondary = PoolBlue,
    onSecondary = PoolText,
    tertiary = PoolSand,
    onTertiary = PoolBg,
    background = PoolBg,
    onBackground = PoolText,
    surface = PoolCard,
    onSurface = PoolText,
    surfaceVariant = PoolAccent,
    onSurfaceVariant = PoolMuted,
    error = PoolRed,
    onError = Color.White,
    outline = PoolBorder
)

val PoolTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = JakartaFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        color = PoolText
    ),
    titleLarge = TextStyle(
        fontFamily = JakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = PoolText
    ),
    titleMedium = TextStyle(
        fontFamily = JakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = PoolText
    ),
    bodyMedium = TextStyle(
        fontFamily = JakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = PoolText
    ),
    bodySmall = TextStyle(
        fontFamily = JakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        color = PoolMuted
    ),
    labelSmall = TextStyle(
        fontFamily = JakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        color = PoolCyan
    )
)

val PoolShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun PoolPulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PoolDarkColorScheme,
        typography = PoolTypography,
        shapes = PoolShapes,
        content = content
    )
}
