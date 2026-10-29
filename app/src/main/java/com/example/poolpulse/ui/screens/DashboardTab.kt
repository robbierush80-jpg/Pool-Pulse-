package com.example.poolpulse.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poolpulse.model.PrescriptionItem
import com.example.poolpulse.model.SeasonMode
import com.example.poolpulse.model.VitalConfig
import com.example.poolpulse.model.WeatherMode
import com.example.poolpulse.ui.components.AnimatedGraffitiHeroCard
import com.example.poolpulse.ui.components.AnimatedGraffitiPoolDocAvatar
import com.example.poolpulse.ui.theme.PoolAccent
import com.example.poolpulse.ui.theme.PoolBg
import com.example.poolpulse.ui.theme.PoolBlue
import com.example.poolpulse.ui.theme.PoolBorder
import com.example.poolpulse.ui.theme.PoolCard
import com.example.poolpulse.ui.theme.PoolCyan
import com.example.poolpulse.ui.theme.PoolGreen
import com.example.poolpulse.ui.theme.PoolMuted
import com.example.poolpulse.ui.theme.PoolOrange
import com.example.poolpulse.ui.theme.PoolRed
import com.example.poolpulse.ui.theme.PoolSand
import com.example.poolpulse.ui.theme.PoolText
import com.example.poolpulse.ui.theme.PoolTrackBg
import com.example.poolpulse.viewmodel.PoolPulseUiState
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardTab(
    state: PoolPulseUiState,
    onWeatherModeChange: (WeatherMode) -> Unit,
    onVitalValueChange: (String, Double) -> Unit,
    onOpenRangeEdit: (String) -> Unit,
    onApplyDose: (PrescriptionItem) -> Unit,
    onAskPoolDoc: (String) -> Unit,
    onOpenSeason: (SeasonMode) -> Unit
) {
    val rxCount = state.prescriptions.size
    val wxLine = if (state.weatherBuffer > 0.0) {
        "⛈️ Check skimmer & pre-buffer FC for weather"
    } else {
        "☀️ Routine skimmer & filter check"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Compact Coastal Hero Card
        AnimatedGraffitiHeroCard(
            waterScore = state.waterHealthScore,
            adjustmentsCount = rxCount,
            notificationsPaused = state.notificationsPaused,
            tomorrowTasks = state.tasks.take(2).map { it.name to it.time },
            weatherLine = wxLine
        )

        // 2. Dynamic 3-Day Forecast Outlook & Proactive Advisor (background changes with weather)
        var wxMenuExpanded by remember { mutableStateOf(false) }
        val isRainWeather = state.weatherMode == WeatherMode.RAIN ||
            state.forecastDays.firstOrNull()?.icon?.contains("🌧") == true ||
            state.forecastDays.firstOrNull()?.icon?.contains("⛈") == true
        val isHeatWeather = state.weatherMode == WeatherMode.HEAT ||
            state.forecastDays.firstOrNull()?.icon?.contains("🔥") == true

        val wxGradientColors = when {
            isRainWeather -> listOf(Color(0xFF1D283A), Color(0xFF0F172A))
            isHeatWeather -> listOf(Color(0xFF432314), Color(0xFF1A2238))
            else -> listOf(Color(0xFF123E66), Color(0xFF132742))
        }
        val wxBorderColor = when {
            isRainWeather -> Color(0xFF60A5FA).copy(alpha = 0.45f)
            isHeatWeather -> PoolOrange.copy(alpha = 0.55f)
            else -> PoolCyan.copy(alpha = 0.35f)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(colors = wxGradientColors))
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    when {
                        isRainWeather -> {
                            // Cartoon Storm Clouds & Raindrops Background
                            val cloudColor = Color(0xFF334155).copy(alpha = 0.55f)
                            drawCircle(color = cloudColor, radius = 26.dp.toPx(), center = Offset(w * 0.78f, h * 0.22f))
                            drawCircle(color = cloudColor, radius = 32.dp.toPx(), center = Offset(w * 0.86f, h * 0.20f))
                            drawCircle(color = cloudColor, radius = 22.dp.toPx(), center = Offset(w * 0.93f, h * 0.25f))
                            // Diagonal cartoon rain streaks
                            for (r in 0..9) {
                                val rx = w * (0.12f + r * 0.088f)
                                val ry = h * (0.18f + (r % 3) * 0.22f)
                                drawLine(
                                    color = Color(0xFF60A5FA).copy(alpha = 0.32f),
                                    start = Offset(rx, ry),
                                    end = Offset(rx - 8.dp.toPx(), ry + 16.dp.toPx()),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        isHeatWeather -> {
                            // Blazing Cartoon Sun & Heatwave Shimmer Background
                            val sunCenter = Offset(w * 0.86f, h * 0.24f)
                            drawCircle(
                                color = Color(0xFFF59E0B).copy(alpha = 0.22f),
                                radius = 44.dp.toPx(),
                                center = sunCenter
                            )
                            drawCircle(
                                color = Color(0xFFFBBF24).copy(alpha = 0.38f),
                                radius = 26.dp.toPx(),
                                center = sunCenter
                            )
                            for (ray in 0 until 8) {
                                val angle = (ray * 45f) * (PI.toFloat() / 180f)
                                drawLine(
                                    color = Color(0xFFFDE68A).copy(alpha = 0.35f),
                                    start = Offset(
                                        sunCenter.x + cos(angle) * 30.dp.toPx(),
                                        sunCenter.y + sin(angle) * 30.dp.toPx()
                                    ),
                                    end = Offset(
                                        sunCenter.x + cos(angle) * 42.dp.toPx(),
                                        sunCenter.y + sin(angle) * 42.dp.toPx()
                                    ),
                                    strokeWidth = 2.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        else -> {
                            // Sunny / Partly Cloudy Coastal Sky Background
                            val sunCenter = Offset(w * 0.84f, h * 0.22f)
                            drawCircle(
                                color = Color(0xFFFDE047).copy(alpha = 0.26f),
                                radius = 24.dp.toPx(),
                                center = sunCenter
                            )
                            // Fluffy cartoon white/sky clouds
                            val cloudCol = Color(0xFFBAE6FD).copy(alpha = 0.20f)
                            drawCircle(color = cloudCol, radius = 18.dp.toPx(), center = Offset(w * 0.75f, h * 0.28f))
                            drawCircle(color = cloudCol, radius = 22.dp.toPx(), center = Offset(w * 0.82f, h * 0.26f))
                            drawCircle(color = cloudCol, radius = 16.dp.toPx(), center = Offset(w * 0.89f, h * 0.29f))
                        }
                    }
                }
                .border(1.dp, wxBorderColor, RoundedCornerShape(14.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 3-Day Forecast Outlook",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = PoolCyan
                )
                Box {
                    Surface(
                        color = PoolBg,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PoolBorder),
                        modifier = Modifier
                            .testTag("weather_mode_selector")
                            .clickable { wxMenuExpanded = true }
                    ) {
                        Text(
                            text = "${state.weatherMode.label} ▾",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PoolText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = wxMenuExpanded,
                        onDismissRequest = { wxMenuExpanded = false },
                        modifier = Modifier.background(PoolCard)
                    ) {
                        WeatherMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.label, color = PoolText, fontSize = 12.sp) },
                                onClick = {
                                    onWeatherModeChange(mode)
                                    wxMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.forecastDays.forEach { day ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(PoolBg.copy(alpha = 0.6f))
                            .border(1.dp, PoolBorder, RoundedCornerShape(9.dp))
                            .padding(vertical = 6.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(day.dayLabel, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = PoolMuted)
                            Text(
                                day.tempLabel,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PoolText
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(day.icon, fontSize = 15.sp)
                            Text("💧${day.precipLabel}", fontSize = 9.sp, color = PoolCyan)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(PoolCyan.copy(alpha = 0.1f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(34.dp)
                        .background(PoolSand)
                )
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                    Text(
                        text = "⚡ PULSE PROACTIVE ADVISOR",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PoolSand
                    )
                    Text(
                        text = state.advisorText,
                        fontSize = 10.5.sp,
                        color = PoolText
                    )
                }
            }
        }

        // 3. Water Vitals Section with Unified Rolling Cartoon Wave across all 6 cards
        SectionHeader(
            title = "WATER VITALS",
            subtitle = "Tap ⚙ Range to Customize Targets"
        )

        state.vitals.forEachIndexed { index, vital ->
            VitalCard(
                cardIndex = index,
                totalCards = state.vitals.size,
                vital = vital,
                onValueChange = { onVitalValueChange(vital.id, it) },
                onEditRange = { onOpenRangeEdit(vital.id) }
            )
        }

        // 4. Smart Prescription Section (with integrated CSI Saturation Badge & Bar)
        SectionHeader(
            title = "SMART PRESCRIPTION",
            subtitle = "$rxCount Action${if (rxCount == 1) "" else "s"}"
        )

        SmartPrescriptionWithCsiCard(
            csi = state.csiValue,
            volumeGallons = state.volumeGallons,
            prescriptions = state.prescriptions,
            onApplyDose = onApplyDose
        )

        // 5. Compact Pool Doc Section with Red Cross Emblem in Background
        SectionHeader(
            title = "POOL DOC & CARE ASSISTANT",
            subtitle = "Reads Your Live Vitals"
        )

        var customQuestion by remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF162C46), PoolCard)
                    )
                )
                .drawBehind {
                    // Symmetric American Red Cross Emblem (just the cross) in the background
                    val cx = size.width * 0.82f
                    val cy = size.height * 0.48f
                    val armLength = 68.dp.toPx()
                    val armThickness = 22.dp.toPx()
                    val borderPad = 2.5.dp.toPx()

                    // Subtle crisp white emblem halo
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.16f),
                        topLeft = Offset(cx - armLength / 2f - borderPad, cy - armThickness / 2f - borderPad),
                        size = Size(armLength + borderPad * 2f, armThickness + borderPad * 2f),
                        cornerRadius = CornerRadius(3.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.16f),
                        topLeft = Offset(cx - armThickness / 2f - borderPad, cy - armLength / 2f - borderPad),
                        size = Size(armThickness + borderPad * 2f, armLength + borderPad * 2f),
                        cornerRadius = CornerRadius(3.dp.toPx())
                    )

                    // Red Cross Horizontal & Vertical Bars
                    val crossRed = Color(0xFFE11D48).copy(alpha = 0.34f)
                    drawRoundRect(
                        color = crossRed,
                        topLeft = Offset(cx - armLength / 2f, cy - armThickness / 2f),
                        size = Size(armLength, armThickness),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )
                    drawRoundRect(
                        color = crossRed,
                        topLeft = Offset(cx - armThickness / 2f, cy - armLength / 2f),
                        size = Size(armThickness, armLength),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )
                }
                .border(1.dp, PoolCyan.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedGraffitiPoolDocAvatar()

                AnimatedContent(
                    targetState = state.poolDocReply,
                    modifier = Modifier.weight(1f),
                    label = "doc_reply"
                ) { replyText ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(PoolBg.copy(alpha = 0.7f))
                            .border(1.dp, PoolBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "🩺 Pool Doc: $replyText",
                            fontSize = 11.sp,
                            color = PoolText
                        )
                    }
                }
            }

            val chips = listOf(
                "cloudy" to "☁️ Cloudy Water",
                "algae" to "🦠 Green / Algae",
                "foam" to "🫧 Foamy Water",
                "burn" to "👀 Burning Eyes",
                "shock" to "⚡ Shock Guide"
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                chips.forEach { (key, label) ->
                    Surface(
                        color = PoolAccent,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PoolBorder),
                        modifier = Modifier
                            .testTag("pool_doc_chip_$key")
                            .clickable { onAskPoolDoc(key) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PoolCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customQuestion,
                    onValueChange = { customQuestion = it },
                    placeholder = {
                        Text(
                            "Type a question for Pool Doc...",
                            fontSize = 11.5.sp,
                            color = PoolMuted
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PoolBg,
                        unfocusedContainerColor = PoolBg,
                        focusedBorderColor = PoolCyan,
                        unfocusedBorderColor = PoolBorder,
                        focusedTextColor = PoolText,
                        unfocusedTextColor = PoolText
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pool_doc_input")
                )
                Button(
                    onClick = { onAskPoolDoc(customQuestion) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PoolCyan,
                        contentColor = PoolBg
                    ),
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier.testTag("pool_doc_go_button")
                ) {
                    Text(
                        "Ask",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 6. Open / Close Pool Season Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = PoolCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PoolSand.copy(alpha = 0.4f)),
                modifier = Modifier
                    .weight(1f)
                    .testTag("open_pool_button")
                    .clickable { onOpenSeason(SeasonMode.OPEN) }
            ) {
                Text(
                    text = "☀️ Open Pool Guide",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PoolSand,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
            Surface(
                color = PoolCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PoolCyan.copy(alpha = 0.4f)),
                modifier = Modifier
                    .weight(1f)
                    .testTag("close_pool_button")
                    .clickable { onOpenSeason(SeasonMode.CLOSE) }
            ) {
                Text(
                    text = "❄️ Close Pool Guide",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PoolCyan,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PoolMuted
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = PoolCyan
            )
        }
    }
}

private fun formatVitalValue(vital: VitalConfig): String {
    return if (vital.value % 1.0 == 0.0 && vital.id != "pH" && vital.id != "fc") {
        vital.value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", vital.value)
    }
}

/**
 * Water Vital Card with a static, solid cartoonish rolling ocean wave background
 * that connects seamlessly from top card to bottom card as if all 6 cards are one canvas.
 */
@Composable
fun VitalCard(
    cardIndex: Int,
    totalCards: Int,
    vital: VitalConfig,
    onValueChange: (Double) -> Unit,
    onEditRange: () -> Unit
) {
    val inRange = vital.value >= vital.idealMin && vital.value <= vital.idealMax
    val statusText = when {
        inRange -> "IN RANGE"
        vital.value < vital.idealMin -> "LOW"
        else -> "HIGH"
    }
    val statusColor = if (inRange) PoolGreen else PoolRed

    var isFocused by remember { mutableStateOf(false) }
    var textValue by remember(vital.id) { mutableStateOf(formatVitalValue(vital)) }

    LaunchedEffect(vital.value, isFocused) {
        if (!isFocused) {
            textValue = formatVitalValue(vital)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFD9B375))
            .drawBehind {
                val w = size.width
                val h = size.height
                val virtualStride = h + 6.dp.toPx()
                val totalVirtualH = virtualStride * totalCards
                val cardTopGlobalY = cardIndex * virtualStride

                val dryBeachSand = Color(0xFFD9B375)
                val wetShoreSand = Color(0xFFC49A5B)
                val sandSpeckle = Color(0xFFA67B3D)
                val inkNavy = Color(0xFF042B4B)
                val deepBarrelBlue = Color(0xFF03538A)
                val richOceanBlue = Color(0xFF0077C2)
                val brightSurfCyan = Color(0xFF00A8E8)
                val skyAquaStreak = Color(0xFF7CE0FF)
                val icyFoamShadow = Color(0xFFB3EEFF)
                val pureFoamWhite = Color(0xFFFFFFFF)

                // Fill entire non-wave background with the same golden beach sand as the bottom nav bar
                drawRect(color = dryBeachSand)

                // Continuous S-curve flow spine across all 6 cards
                fun spineX(normY: Float): Float {
                    return w * (0.50f + 0.24f * sin(normY * 2.3f * PI.toFloat() - 0.35f))
                }

                val steps = 80
                val wetSandPath = Path()
                val deepBarrelPath = Path()
                val oceanBodyPath = Path()
                val cyanRibbonPath = Path()
                val skyStreakPath = Path()
                val smoothFoamCrestPath = Path()
                val innerFoamShadowPath = Path()

                for (i in 0..steps) {
                    val gy = (i.toFloat() / steps) * totalVirtualH
                    val normY = gy / totalVirtualH
                    val ly = gy - cardTopGlobalY
                    val sx = spineX(normY)

                    // Smooth rolling wave swell (no jagged teeth)
                    val swell = w * 0.045f * sin(normY * 4.6f * PI.toFloat())
                    val xFoamEdge = sx + swell
                    val xWetSand = xFoamEdge - w * 0.068f
                    val xBarrel = xFoamEdge + w * 0.055f
                    val xOcean = xFoamEdge + w * 0.135f
                    val xCyan = xFoamEdge + w * 0.215f

                    if (i == 0) {
                        wetSandPath.moveTo(xWetSand, ly)
                        deepBarrelPath.moveTo(xFoamEdge, ly)
                        smoothFoamCrestPath.moveTo(xFoamEdge, ly)
                        innerFoamShadowPath.moveTo(xFoamEdge + 4.dp.toPx(), ly)
                        oceanBodyPath.moveTo(xBarrel, ly)
                        cyanRibbonPath.moveTo(xOcean, ly)
                        skyStreakPath.moveTo(xCyan, ly)
                    } else {
                        wetSandPath.lineTo(xWetSand, ly)
                        deepBarrelPath.lineTo(xFoamEdge, ly)
                        smoothFoamCrestPath.lineTo(xFoamEdge, ly)
                        innerFoamShadowPath.lineTo(xFoamEdge + 4.dp.toPx(), ly)
                        oceanBodyPath.lineTo(xBarrel, ly)
                        cyanRibbonPath.lineTo(xOcean, ly)
                        skyStreakPath.lineTo(xCyan, ly)
                    }
                }

                val bottomLy = totalVirtualH - cardTopGlobalY + 20f
                val topLy = -cardTopGlobalY - 20f

                listOf(wetSandPath, deepBarrelPath, oceanBodyPath, cyanRibbonPath, skyStreakPath).forEach { p ->
                    p.lineTo(w + 30f, bottomLy)
                    p.lineTo(w + 30f, topLy)
                    p.close()
                }

                // Subtle sand speckles on the dry sand beach side
                for (g in 0..5) {
                    val gx = w * (0.06f + (g * 0.055f))
                    val gy = h * (0.20f + ((g * 37 + cardIndex * 19) % 60) / 100f)
                    drawCircle(
                        color = sandSpeckle.copy(alpha = 0.35f),
                        radius = 1.4.dp.toPx(),
                        center = Offset(gx, gy)
                    )
                }

                // Wet shoreline sand strip right along the wave crest (matching bottom beach nav)
                drawPath(path = wetSandPath, color = wetShoreSand)

                // 1. Multi-toned Solid Cartoon Wave Body Layers
                drawPath(path = deepBarrelPath, color = deepBarrelBlue)
                drawPath(path = oceanBodyPath, color = richOceanBlue)
                drawPath(path = cyanRibbonPath, color = brightSurfCyan)
                drawPath(path = skyStreakPath, color = skyAquaStreak.copy(alpha = 0.85f))

                // 2. Smooth Curved Water Flow Ribbons inside the wave body
                for (s in 0..9) {
                    val segNormStart = s / 10f
                    val segNormEnd = (s + 0.70f) / 10f
                    val streakPath = Path()
                    val subSteps = 12
                    val offsetFrac = if (s % 3 == 0) 0.04f else if (s % 3 == 1) 0.11f else 0.18f
                    val streakColor = when (s % 3) {
                        0 -> brightSurfCyan
                        1 -> skyAquaStreak
                        else -> pureFoamWhite.copy(alpha = 0.85f)
                    }
                    for (k in 0..subSteps) {
                        val ny = segNormStart + (segNormEnd - segNormStart) * (k.toFloat() / subSteps)
                        val gy = ny * totalVirtualH
                        val ly = gy - cardTopGlobalY
                        val swell = w * 0.045f * sin(ny * 4.6f * PI.toFloat())
                        val sx = spineX(ny) + swell + w * offsetFrac
                        if (k == 0) streakPath.moveTo(sx, ly) else streakPath.lineTo(sx, ly)
                    }
                    drawPath(
                        path = streakPath,
                        color = streakColor,
                        style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 3. Cute Cartoon Dolphin Poking His Head Out of the Wave (around Card 2 / normY = 0.27f)
                val dolphinGlobalY = totalVirtualH * 0.27f
                val dLy = dolphinGlobalY - cardTopGlobalY
                if (dLy in -60f..(h + 60f)) {
                    val dNormY = 0.27f
                    val dWaveX = spineX(dNormY) + w * 0.045f * sin(dNormY * 4.6f * PI.toFloat())
                    val dX = dWaveX - 10.dp.toPx()
                    val rScale = 1.dp.toPx()

                    // Dolphin body & arched head poking out to the left of the wave crest
                    val dolphinBody = Path().apply {
                        moveTo(dX + 18f * rScale, dLy + 14f * rScale)
                        cubicTo(
                            dX + 6f * rScale, dLy - 12f * rScale,
                            dX - 14f * rScale, dLy - 16f * rScale,
                            dX - 20f * rScale, dLy - 4f * rScale
                        )
                        // Cute bottle-nose snout / beak
                        quadraticTo(
                            dX - 30f * rScale, dLy - 2f * rScale,
                            dX - 27f * rScale, dLy + 3f * rScale
                        )
                        quadraticTo(
                            dX - 20f * rScale, dLy + 5f * rScale,
                            dX - 14f * rScale, dLy + 4f * rScale
                        )
                        // Throat / chest curving back into the wave
                        quadraticTo(
                            dX - 6f * rScale, dLy + 12f * rScale,
                            dX + 8f * rScale, dLy + 18f * rScale
                        )
                        close()
                    }
                    // Dorsal fin peeking up on the dolphin's back
                    val dorsalFin = Path().apply {
                        moveTo(dX + 2f * rScale, dLy - 6f * rScale)
                        quadraticTo(
                            dX + 8f * rScale, dLy - 17f * rScale,
                            dX + 14f * rScale, dLy - 13f * rScale
                        )
                        quadraticTo(
                            dX + 9f * rScale, dLy - 7f * rScale,
                            dX + 8f * rScale, dLy - 2f * rScale
                        )
                        close()
                    }

                    // Cartoon navy outline + sleek dolphin blue fill
                    drawPath(
                        path = dorsalFin,
                        color = inkNavy,
                        style = Stroke(width = 3.2f * rScale, join = StrokeJoin.Round)
                    )
                    drawPath(path = dorsalFin, color = Color(0xFF2563EB))

                    drawPath(
                        path = dolphinBody,
                        color = inkNavy,
                        style = Stroke(width = 3.5f * rScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawPath(path = dolphinBody, color = Color(0xFF3B82F6))

                    // Light sky-cream underbelly / chin highlight
                    val bellyPath = Path().apply {
                        moveTo(dX - 25f * rScale, dLy + 2f * rScale)
                        quadraticTo(
                            dX - 15f * rScale, dLy + 4f * rScale,
                            dX - 5f * rScale, dLy + 13f * rScale
                        )
                    }
                    drawPath(
                        path = bellyPath,
                        color = Color(0xFFE0F2FE),
                        style = Stroke(width = 3.5f * rScale, cap = StrokeCap.Round)
                    )

                    // Cheerful cartoon eye & smile
                    drawCircle(
                        color = Color.White,
                        radius = 2.8f * rScale,
                        center = Offset(dX - 15f * rScale, dLy - 4f * rScale)
                    )
                    drawCircle(
                        color = inkNavy,
                        radius = 1.6f * rScale,
                        center = Offset(dX - 15.5f * rScale, dLy - 4f * rScale)
                    )
                    // Little water splash droplets around the dolphin
                    drawCircle(
                        color = pureFoamWhite,
                        radius = 2.2f * rScale,
                        center = Offset(dX - 31f * rScale, dLy - 7f * rScale)
                    )
                    drawCircle(
                        color = skyAquaStreak,
                        radius = 1.8f * rScale,
                        center = Offset(dX - 26f * rScale, dLy - 12f * rScale)
                    )
                }

                // 4. Clean, Smooth Cartoon White Foam Crest (no jagged "teeth")
                drawPath(
                    path = smoothFoamCrestPath,
                    color = inkNavy,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = innerFoamShadowPath,
                    color = icyFoamShadow,
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = smoothFoamCrestPath,
                    color = pureFoamWhite,
                    style = Stroke(width = 8.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            .border(
                1.dp,
                if (inRange) PoolCyan.copy(alpha = 0.35f) else PoolRed.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val crispCardTextShadow = Shadow(
            color = Color(0xFF04223A),
            offset = Offset(0f, 1.5f),
            blurRadius = 3.5f
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = vital.name,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    style = TextStyle(shadow = crispCardTextShadow),
                    modifier = Modifier.weight(1f, fill = false)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF071E34).copy(alpha = 0.90f))
                        .border(1.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(5.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Text(
                text = "Ideal: ${vital.idealMin}–${vital.idealMax} | Target: ${vital.target} ${vital.unit}",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF8FAFC),
                style = TextStyle(shadow = crispCardTextShadow)
            )

            RangeBarTrack(
                value = vital.value,
                min = vital.min,
                max = vital.max,
                idealMin = vital.idealMin,
                idealMax = vital.idealMax,
                target = vital.target
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${vital.min}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = TextStyle(shadow = crispCardTextShadow)
                )
                Text(
                    "▲ Target: ${vital.target}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFDE047),
                    style = TextStyle(shadow = crispCardTextShadow)
                )
                Text(
                    "${vital.max} ${vital.unit}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = TextStyle(shadow = crispCardTextShadow)
                )
            }
        }

        // Compact Right Column (No + / - buttons so the card is shorter & cleaner)
        Column(
            modifier = Modifier.width(96.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                color = PoolAccent,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PoolCyan.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_range_${vital.id}")
                    .clickable { onEditRange() }
            ) {
                Text(
                    text = "⚙ Edit Range",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = PoolCyan,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 2.5.dp, horizontal = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PoolBg.copy(alpha = 0.92f))
                    .border(1.dp, statusColor.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                BasicTextField(
                    value = textValue,
                    onValueChange = { raw ->
                        textValue = raw
                        raw.toDoubleOrNull()?.let { parsed ->
                            onValueChange(parsed)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    cursorBrush = SolidColor(PoolCyan),
                    textStyle = TextStyle(
                        color = PoolText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                            if (!focusState.isFocused) {
                                val parsed = textValue.toDoubleOrNull()
                                if (parsed != null) {
                                    onValueChange(parsed)
                                } else {
                                    textValue = formatVitalValue(vital)
                                }
                            }
                        }
                        .testTag("vital_input_${vital.id}")
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = vital.unit,
                    fontSize = 9.5.sp,
                    color = PoolMuted
                )
            }
        }
    }
}

/**
 * Smart Prescription Card with integrated Calcite Saturation Index (CSI) readout,
 * multi-toned cartoon pool water filling the entire background, and a large, bold
 * cel-shaded cartoon Red-and-White Striped Pool Float.
 */
@Composable
fun SmartPrescriptionWithCsiCard(
    csi: Double,
    volumeGallons: Int,
    prescriptions: List<PrescriptionItem>,
    onApplyDose: (PrescriptionItem) -> Unit
) {
    val csiBalanced = csi in -0.30..0.30
    val csiStatusText = when {
        csiBalanced -> "BALANCED"
        csi < -0.30 -> "CORROSIVE"
        else -> "SCALING"
    }
    val csiColor = if (csiBalanced) PoolGreen else PoolRed
    val formattedCsi = String.format(Locale.US, "%s%.2f", if (csi > 0) "+" else "", csi)

    val prescTextShadow = Shadow(
        color = Color(0xFF04223A),
        offset = Offset(0f, 1.5f),
        blurRadius = 3.5f
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0077C2))
            .drawBehind {
                val w = size.width
                val h = size.height
                val inkNavy = Color(0xFF042B4B)
                val deepBarrelBlue = Color(0xFF03538A)
                val richOceanBlue = Color(0xFF0077C2)
                val brightSurfCyan = Color(0xFF00A8E8)
                val skyAquaStreak = Color(0xFF7CE0FF)

                // 1. Cartoon Pool Water Background filling all leftover space on the card
                drawRect(color = richOceanBlue)

                // Layered cartoon water wave bands across the card
                val deepBand = Path().apply {
                    moveTo(0f, 0f)
                    quadraticTo(w * 0.35f, h * 0.28f, w * 0.72f, 0f)
                    close()
                }
                drawPath(path = deepBand, color = deepBarrelBlue)

                val midCyanBand = Path().apply {
                    moveTo(0f, h * 0.32f)
                    cubicTo(w * 0.30f, h * 0.12f, w * 0.65f, h * 0.56f, w, h * 0.24f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path = midCyanBand, color = brightSurfCyan)

                val lowerAquaBand = Path().apply {
                    moveTo(0f, h * 0.68f)
                    cubicTo(w * 0.32f, h * 0.48f, w * 0.68f, h * 0.88f, w, h * 0.60f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path = lowerAquaBand, color = skyAquaStreak.copy(alpha = 0.55f))

                // Cartoon water flow streaks & caustic ribbons across the water filler
                for (r in 0..3) {
                    val ry = h * (0.20f + r * 0.22f)
                    val ribbon = Path().apply {
                        moveTo(w * 0.04f, ry)
                        quadraticTo(w * 0.24f, ry - 8.dp.toPx(), w * 0.44f, ry + 2.dp.toPx())
                        quadraticTo(w * 0.64f, ry + 10.dp.toPx(), w * 0.94f, ry - 4.dp.toPx())
                    }
                    drawPath(
                        path = ribbon,
                        color = if (r % 2 == 0) skyAquaStreak.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.45f),
                        style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 2. Big, Bold Cel-Shaded Cartoon Red-and-White Striped Pool Ring Float
                val fx = w * 0.75f
                val fy = h * 0.54f
                val outerR = (h * 0.46f).coerceIn(56.dp.toPx(), 78.dp.toPx())
                val innerR = outerR * 0.38f
                val outlineW = 3.2.dp.toPx()

                // Outer cartoon white & sky-aqua water splash rings around the big float
                drawCircle(
                    color = skyAquaStreak.copy(alpha = 0.85f),
                    radius = outerR + 11.dp.toPx(),
                    center = Offset(fx, fy),
                    style = Stroke(width = 2.8.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.90f),
                    radius = outerR + 5.5.dp.toPx(),
                    center = Offset(fx, fy),
                    style = Stroke(width = 2.4.dp.toPx())
                )

                // Bold cartoon 3D drop shadow in the water under the float
                drawCircle(
                    color = inkNavy.copy(alpha = 0.55f),
                    radius = outerR + 2.dp.toPx(),
                    center = Offset(fx + 3.dp.toPx(), fy + 4.dp.toPx())
                )

                // Dark navy cartoon outline around outer float
                drawCircle(
                    color = inkNavy,
                    radius = outerR + outlineW,
                    center = Offset(fx, fy)
                )

                // Crisp solid white base ring of the cartoon float
                drawCircle(
                    color = Color.White,
                    radius = outerR,
                    center = Offset(fx, fy)
                )

                // Subtle icy blue inner-rim 3D cartoon shading on the white segments
                drawCircle(
                    color = Color(0xFFD8F3FF),
                    radius = outerR * 0.92f,
                    center = Offset(fx, fy),
                    style = Stroke(width = 4.dp.toPx())
                )

                // 4 Bold Cartoon Red Stripes with cel-shaded depth & navy separator lines
                val stripeAngles = listOf(-22f, 68f, 158f, 248f)
                for (angle in stripeAngles) {
                    // Main vibrant cartoon red stripe
                    drawArc(
                        color = Color(0xFFEF4444),
                        startAngle = angle,
                        sweepAngle = 45f,
                        useCenter = true,
                        topLeft = Offset(fx - outerR, fy - outerR),
                        size = Size(outerR * 2f, outerR * 2f)
                    )
                    // Cel-shaded darker crimson rim shadow along outer edge of red stripe
                    drawArc(
                        color = Color(0xFFDC2626),
                        startAngle = angle,
                        sweepAngle = 45f,
                        useCenter = false,
                        topLeft = Offset(fx - outerR + 3.dp.toPx(), fy - outerR + 3.dp.toPx()),
                        size = Size((outerR - 3.dp.toPx()) * 2f, (outerR - 3.dp.toPx()) * 2f),
                        style = Stroke(width = 5.dp.toPx())
                    )
                    // Bold cartoon navy seam lines along each stripe edge
                    for (edgeDeg in listOf(angle, angle + 45f)) {
                        val rad = edgeDeg * (PI.toFloat() / 180f)
                        drawLine(
                            color = inkNavy,
                            start = Offset(fx + cos(rad) * innerR, fy + sin(rad) * innerR),
                            end = Offset(fx + cos(rad) * outerR, fy + sin(rad) * outerR),
                            strokeWidth = 2.4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Glossy cartoon specular shine arc on top-left of the tube
                drawArc(
                    color = Color.White.copy(alpha = 0.85f),
                    startAngle = 200f,
                    sweepAngle = 55f,
                    useCenter = false,
                    topLeft = Offset(fx - outerR * 0.72f, fy - outerR * 0.72f),
                    size = Size(outerR * 1.44f, outerR * 1.44f),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Inner hole of the float showing pool water + inner tube wall shadow + bold navy outline
                drawCircle(
                    color = inkNavy,
                    radius = innerR + outlineW,
                    center = Offset(fx, fy)
                )
                drawCircle(
                    color = deepBarrelBlue,
                    radius = innerR,
                    center = Offset(fx, fy)
                )
                drawCircle(
                    color = brightSurfCyan,
                    radius = innerR * 0.82f,
                    center = Offset(fx, fy + 2.dp.toPx())
                )
            }
            .border(1.5.dp, Color(0xFF042B4B), RoundedCornerShape(14.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // Integrated CSI Saturation Header Bar inside Smart Prescription Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFF071E34).copy(alpha = 0.74f))
                .border(1.dp, PoolBorder, RoundedCornerShape(9.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Calcite Saturation Index (CSI)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Protection Zone: -0.30 to +0.30 • Target: 0.00",
                    fontSize = 9.5.sp,
                    color = Color(0xFFE2E8F0)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$formattedCsi CSI",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PoolCyan
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF041526).copy(alpha = 0.9f))
                        .border(1.dp, csiColor.copy(alpha = 0.6f), RoundedCornerShape(5.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = csiStatusText,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = csiColor
                    )
                }
            }
        }

        if (prescriptions.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF071E34).copy(alpha = 0.88f))
                        .border(1.dp, PoolGreen.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "READY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PoolGreen
                    )
                }
                Column {
                    Text(
                        text = "✓ All Chemistry & Weather Buffers Met",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.5.sp,
                        color = Color.White,
                        style = TextStyle(shadow = prescTextShadow)
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "Zero dosing needed for your %,d-gal pool.",
                            volumeGallons
                        ),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF8FAFC),
                        style = TextStyle(shadow = prescTextShadow)
                    )
                }
            }
        } else {
            prescriptions.forEachIndexed { index, rx ->
                PrescriptionItemRow(
                    stepNumber = index + 1,
                    rx = rx,
                    onApply = { onApplyDose(rx) }
                )
            }
        }
    }
}

@Composable
fun RangeBarTrack(
    value: Double,
    min: Double,
    max: Double,
    idealMin: Double,
    idealMax: Double,
    target: Double
) {
    val span = (max - min).coerceAtLeast(0.001)
    fun pct(v: Double): Float = (((v - min) / span).toFloat()).coerceIn(0.04f, 0.96f)

    val iMinPct = pct(idealMin)
    val iMaxPct = pct(idealMax)
    val targetPct = pct(target)
    val valPct = pct(value)
    val isOk = value in idealMin..idealMax

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .padding(vertical = 2.dp)
    ) {
        val w = size.width
        val h = size.height
        val trackHeight = 8.dp.toPx()
        val trackTop = (h - trackHeight) / 2f

        drawRoundRect(
            color = PoolTrackBg,
            topLeft = Offset(0f, trackTop),
            size = Size(w, trackHeight),
            cornerRadius = CornerRadius(4.dp.toPx())
        )

        val zoneLeft = w * iMinPct
        val zoneWidth = (w * (iMaxPct - iMinPct)).coerceAtLeast(6.dp.toPx())
        drawRect(
            color = PoolGreen.copy(alpha = 0.28f),
            topLeft = Offset(zoneLeft, trackTop),
            size = Size(zoneWidth, trackHeight)
        )

        val targetX = w * targetPct
        drawRoundRect(
            color = PoolOrange,
            topLeft = Offset(targetX - 1.5.dp.toPx(), 0f),
            size = Size(3.dp.toPx(), h),
            cornerRadius = CornerRadius(1.5.dp.toPx())
        )

        val dotX = w * valPct
        val dotColor = if (isOk) PoolGreen else PoolRed
        drawCircle(
            color = PoolBg,
            radius = 6.5.dp.toPx(),
            center = Offset(dotX, h / 2f)
        )
        drawCircle(
            color = dotColor,
            radius = 5.dp.toPx(),
            center = Offset(dotX, h / 2f)
        )
    }
}

@Composable
fun PrescriptionItemRow(
    stepNumber: Int,
    rx: PrescriptionItem,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PoolBg.copy(alpha = 0.45f))
            .border(1.dp, PoolBorder, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(PoolSand)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "STEP $stepNumber",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PoolBg
                        )
                    }
                    Text(
                        text = rx.title,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PoolText
                    )
                }

                Text(
                    text = rx.description,
                    fontSize = 10.5.sp,
                    color = PoolMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(rx.chemical, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = PoolCyan)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format(
                                Locale.US,
                                "Est. $%.2f • -%.2f %s",
                                rx.estimatedCost,
                                rx.quantity,
                                rx.unit
                            ),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PoolGreen
                        )
                    }
                }
            }

            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PoolCyan,
                    contentColor = PoolBg
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("apply_dose_${rx.inventoryKey}")
            ) {
                Text(
                    text = "Apply & Deduct",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = rx.howToApply,
            fontSize = 9.5.sp,
            color = PoolMuted
        )
    }
}
