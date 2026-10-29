package com.example.poolpulse.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poolpulse.ui.theme.PoolAccent
import com.example.poolpulse.ui.theme.PoolBg
import com.example.poolpulse.ui.theme.PoolBlue
import com.example.poolpulse.ui.theme.PoolBorder
import com.example.poolpulse.ui.theme.PoolCard
import com.example.poolpulse.ui.theme.PoolCyan
import com.example.poolpulse.ui.theme.PoolGreen
import com.example.poolpulse.ui.theme.PoolHeroStart
import com.example.poolpulse.ui.theme.PoolMuted
import com.example.poolpulse.ui.theme.PoolOrange
import com.example.poolpulse.ui.theme.PoolRed
import com.example.poolpulse.ui.theme.PoolSand
import com.example.poolpulse.ui.theme.PoolText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Compact Coastal Hero Card with a Big Round Cartoon Pool spanning the whole background,
 * a Water Health Score Ring, a compact 3-Pillar Coach bar, and Tomorrow's Outlook.
 */
@Composable
fun AnimatedGraffitiHeroCard(
    waterScore: Int,
    adjustmentsCount: Int,
    notificationsPaused: Boolean,
    tomorrowTasks: List<Pair<String, String>>,
    weatherLine: String
) {
    val infinite = rememberInfiniteTransition(label = "coastal_pool")
    val wavePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )
    val floatDrift by infinite.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_drift"
    )

    var selectedPillar by remember { mutableIntStateOf(0) }

    val animatedScore by animateFloatAsState(
        targetValue = waterScore.toFloat(),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "water_score"
    )

    val scoreColor = when {
        waterScore >= 90 -> PoolGreen
        waterScore >= 70 -> PoolOrange
        else -> PoolRed
    }

    val heroTextShadow = Shadow(
        color = Color(0xFF04223A),
        offset = Offset(0f, 1.5f),
        blurRadius = 3.5f
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFD9B375))
            .drawBehind {
                drawFullCardCartoonRoundPool(
                    wavePhase = wavePhase,
                    floatDrift = floatDrift
                )
            }
            .border(1.dp, PoolCyan.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("dj_splash_cartoon_stage"),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Top Row: Compact Score Ring + Status Text (Right side open to showcase swimmer, ladder & float in the round pool)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compact Score Gauge (48.dp) with dark badge backing for contrast over pool water
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF071E34).copy(alpha = 0.84f)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
                    val strokeWidth = 4.5.dp.toPx()
                    drawArc(
                        color = PoolBg.copy(alpha = 0.8f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = scoreColor,
                        startAngle = 135f,
                        sweepAngle = 270f * (animatedScore / 100f).coerceIn(0.05f, 1f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${animatedScore.toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scoreColor
                    )
                    Text(
                        text = "SCORE",
                        fontSize = 6.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PoolText
                    )
                }
            }

            // Status & Summary
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = if (adjustmentsCount == 0) {
                        "Water is Balanced"
                    } else {
                        "$adjustmentsCount Adjustment${if (adjustmentsCount > 1) "s" else ""} Ready"
                    },
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    style = TextStyle(shadow = heroTextShadow)
                )
                Text(
                    text = if (adjustmentsCount == 0) {
                        "Vitals & CSI in protection range."
                    } else {
                        "Follow chemical doses below."
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFF1F5F9),
                    style = TextStyle(shadow = heroTextShadow)
                )
            }
        }

        // Compact 3-Pillar Pool Care Strip (Circulation • Cleaning • Chemistry)
        val pillars = listOf(
            Triple(0, "1. Circulation", "Run pump 8–12 hrs/day & angle jets 45° downward."),
            Triple(1, "2. Cleaning", "Skim daily, brush twice weekly & run pool robot."),
            Triple(2, "3. Chemistry", "Balance Alkalinity first, then pH, then Chlorine.")
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF071E34).copy(alpha = 0.68f))
                .border(1.dp, PoolBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pillars.forEach { (idx, label, _) ->
                    val active = selectedPillar == idx
                    Surface(
                        color = if (active) PoolCyan.copy(alpha = 0.28f) else PoolAccent.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(5.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (active) PoolCyan else PoolBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPillar = idx }
                    ) {
                        Text(
                            text = label,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) PoolCyan else PoolText,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = pillars[selectedPillar].third,
                fontSize = 9.5.sp,
                color = PoolText
            )
        }

        // Compact Tomorrow's Maintenance Outlook
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF071E34).copy(alpha = 0.68f))
                .border(1.dp, PoolBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = "📅 TOMORROW'S OUTLOOK",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = PoolSand
            )
            if (notificationsPaused) {
                Text(
                    text = "• 🔕 Notifications Paused until pool opens",
                    fontSize = 10.sp,
                    color = PoolText
                )
            } else {
                val firstTask = tomorrowTasks.firstOrNull()
                if (firstTask != null) {
                    Text(
                        text = "• ${firstTask.first} (${firstTask.second}) • $weatherLine",
                        fontSize = 10.sp,
                        color = PoolText
                    )
                } else {
                    Text(
                        text = "• $weatherLine",
                        fontSize = 10.sp,
                        color = PoolText
                    )
                }
            }
        }
    }
}

/**
 * Draws a Big Round Cartoon Swimming Pool spanning across the whole background of the Hero card,
 * surrounded by golden beach sand filling in the entire card, complete with coping rim, top-rail
 * post caps, inner pool wall shadow, sparkling turquoise water, caustic ripples, pool ladder,
 * lap swimmer, and a floating ring.
 */
private fun DrawScope.drawFullCardCartoonRoundPool(
    wavePhase: Float,
    floatDrift: Float
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.50f
    val cy = h * 0.50f

    val dryBeachSand = Color(0xFFD9B375)
    val wetShoreSand = Color(0xFFC49A5B)
    val sandSpeckle = Color(0xFFA67B3D)
    val inkNavy = Color(0xFF042B4B)
    val copingRimLight = Color(0xFFE2E8F0)
    val copingCapSand = Color(0xFFF3D5A5)
    val wallShadowBlue = Color(0xFF03538A)
    val deepPoolBlue = Color(0xFF0077C2)
    val midPoolCyan = Color(0xFF0096D6)
    val brightCenterAqua = Color(0xFF22B8F0)

    // 0. Fill the entire card background around the pool with golden beach sand
    drawRect(color = dryBeachSand)

    // Subtle cartoon sand speckles in the corners around the round pool
    val cornerDots = listOf(
        Offset(w * 0.06f, h * 0.14f),
        Offset(w * 0.12f, h * 0.08f),
        Offset(w * 0.88f, h * 0.09f),
        Offset(w * 0.94f, h * 0.16f),
        Offset(w * 0.07f, h * 0.86f),
        Offset(w * 0.14f, h * 0.92f),
        Offset(w * 0.87f, h * 0.91f),
        Offset(w * 0.93f, h * 0.84f)
    )
    cornerDots.forEach { pt ->
        drawCircle(
            color = sandSpeckle.copy(alpha = 0.42f),
            radius = 1.6.dp.toPx(),
            center = pt
        )
    }

    // Outer dimensions of the big round cartoon pool spanning the card with sandy beach around it
    val outerRx = w * 0.46f
    val outerRy = h * 0.44f
    val rimWidth = 9.dp.toPx()
    val waterRx = outerRx - rimWidth
    val waterRy = outerRy - rimWidth

    // 1. Warm wet-sand contour & drop shadow around the big round pool rim
    drawOval(
        color = wetShoreSand,
        topLeft = Offset(cx - outerRx - 6.dp.toPx(), cy - outerRy - 4.dp.toPx()),
        size = Size(outerRx * 2f + 12.dp.toPx(), outerRy * 2f + 10.dp.toPx())
    )
    drawOval(
        color = inkNavy.copy(alpha = 0.45f),
        topLeft = Offset(cx - outerRx - 2.dp.toPx(), cy - outerRy + 2.dp.toPx()),
        size = Size(outerRx * 2f + 4.dp.toPx(), outerRy * 2f + 2.dp.toPx())
    )

    // 2. Wide Cartoon Pool Coping / Top-Rail Rim
    drawOval(
        color = copingRimLight,
        topLeft = Offset(cx - outerRx, cy - outerRy),
        size = Size(outerRx * 2f, outerRy * 2f)
    )
    drawOval(
        color = inkNavy,
        topLeft = Offset(cx - outerRx, cy - outerRy),
        size = Size(outerRx * 2f, outerRy * 2f),
        style = Stroke(width = 2.5.dp.toPx())
    )

    // 3. Inner Pool Wall Shadow & Multi-Toned Turquoise Pool Water
    drawOval(
        color = wallShadowBlue,
        topLeft = Offset(cx - waterRx, cy - waterRy),
        size = Size(waterRx * 2f, waterRy * 2f)
    )
    // Offset down slightly so the top inner pool wall shows a 3D cartoon rim shadow
    val wallDepth = 5.dp.toPx()
    drawOval(
        color = deepPoolBlue,
        topLeft = Offset(cx - waterRx + 2.dp.toPx(), cy - waterRy + wallDepth),
        size = Size((waterRx - 2.dp.toPx()) * 2f, (waterRy - wallDepth * 0.6f) * 2f)
    )
    drawOval(
        color = midPoolCyan,
        topLeft = Offset(cx - waterRx * 0.86f, cy - waterRy * 0.80f + wallDepth * 0.5f),
        size = Size(waterRx * 1.72f, waterRy * 1.60f)
    )
    drawOval(
        color = brightCenterAqua.copy(alpha = 0.65f),
        topLeft = Offset(cx - waterRx * 0.62f, cy - waterRy * 0.56f + wallDepth * 0.5f),
        size = Size(waterRx * 1.24f, waterRy * 1.12f)
    )

    // Inner outline of the pool coping rim
    drawOval(
        color = inkNavy,
        topLeft = Offset(cx - waterRx, cy - waterRy),
        size = Size(waterRx * 2f, waterRy * 2f),
        style = Stroke(width = 2.dp.toPx())
    )

    // 4. Cartoon Top-Rail Post Caps around the Round Pool Coping Perimeter
    val midRimRx = (outerRx + waterRx) * 0.5f
    val midRimRy = (outerRy + waterRy) * 0.5f
    for (p in 0 until 12) {
        val ang = (p * 30f) * (PI.toFloat() / 180f)
        val px = cx + cos(ang) * midRimRx
        val py = cy + sin(ang) * midRimRy
        drawCircle(
            color = inkNavy,
            radius = 3.5.dp.toPx(),
            center = Offset(px, py)
        )
        drawCircle(
            color = copingCapSand,
            radius = 2.2.dp.toPx(),
            center = Offset(px, py)
        )
    }

    // 5. Gentle Cartoon Caustic Water Ripples inside the Round Pool
    for (r in -1..1) {
        val ryBase = cy + r * (waterRy * 0.36f)
        val ripplePath = Path().apply {
            val startX = cx - waterRx * 0.72f
            val endX = cx + waterRx * 0.72f
            moveTo(startX, ryBase)
            val segs = 16
            for (s in 1..segs) {
                val frac = s.toFloat() / segs
                val x = startX + (endX - startX) * frac
                val y = ryBase + sin(frac * 3f * PI.toFloat() + wavePhase + r) * 3.dp.toPx()
                lineTo(x, y)
            }
        }
        drawPath(
            path = ripplePath,
            color = Color.White.copy(alpha = 0.25f),
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // 6. Cartoon Chrome Pool Ladder on the top-left/top-right rim
    val ladderX = cx + waterRx * 0.46f
    val ladderTopY = cy - outerRy + 2.dp.toPx()
    val ladderBotY = ladderTopY + 22.dp.toPx()
    val railGap = 9.dp.toPx()
    drawLine(
        color = copingRimLight,
        start = Offset(ladderX, ladderTopY),
        end = Offset(ladderX, ladderBotY),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = copingRimLight,
        start = Offset(ladderX + railGap, ladderTopY + 1.dp.toPx()),
        end = Offset(ladderX + railGap, ladderBotY),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = copingRimLight,
        start = Offset(ladderX + 1.dp.toPx(), ladderTopY + 10.dp.toPx()),
        end = Offset(ladderX + railGap - 1.dp.toPx(), ladderTopY + 10.dp.toPx()),
        strokeWidth = 2.dp.toPx()
    )
    drawLine(
        color = copingRimLight,
        start = Offset(ladderX + 1.dp.toPx(), ladderTopY + 16.dp.toPx()),
        end = Offset(ladderX + railGap - 1.dp.toPx(), ladderTopY + 16.dp.toPx()),
        strokeWidth = 2.dp.toPx()
    )

    // 7. Overhead Lap Swimmer & Classic Ring Float inside the top-right water of the Round Pool
    val swimmerX = (w * 0.66f) + sin(wavePhase * 0.8f) * 6.dp.toPx()
    val swimmerY = h * 0.22f
    drawOval(
        color = PoolSand,
        topLeft = Offset(swimmerX - 6.dp.toPx(), swimmerY - 3.dp.toPx()),
        size = Size(12.dp.toPx(), 6.dp.toPx())
    )
    drawCircle(
        color = inkNavy,
        radius = 3.2.dp.toPx(),
        center = Offset(swimmerX + 5.dp.toPx(), swimmerY)
    )
    drawCircle(
        color = PoolCyan,
        radius = 2.4.dp.toPx(),
        center = Offset(swimmerX + 5.dp.toPx(), swimmerY)
    )

    val fx = w * 0.84f
    val fy = h * 0.24f + floatDrift
    val fRad = 11.dp.toPx()
    rotate(degrees = floatDrift * 3f, pivot = Offset(fx, fy)) {
        drawCircle(
            color = inkNavy,
            radius = fRad + 1.5.dp.toPx(),
            center = Offset(fx, fy)
        )
        drawCircle(
            color = Color.White,
            radius = fRad,
            center = Offset(fx, fy)
        )
        for (a in listOf(0f, 90f, 180f, 270f)) {
            drawArc(
                color = Color(0xFFEF4444),
                startAngle = a,
                sweepAngle = 45f,
                useCenter = true,
                topLeft = Offset(fx - fRad, fy - fRad),
                size = Size(fRad * 2f, fRad * 2f)
            )
        }
        drawCircle(
            color = midPoolCyan,
            radius = fRad * 0.42f,
            center = Offset(fx, fy)
        )
        drawCircle(
            color = inkNavy,
            radius = fRad * 0.42f,
            center = Offset(fx, fy),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

/**
 * Tasteful Animated Autonomous Pool Cleaner Robot for the Pool Doc section.
 */
@Composable
fun AnimatedGraffitiPoolDocAvatar(
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "pool_robot")
    val bubblePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "robot_bubbles"
    )
    val robotGlide by infinite.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "robot_glide"
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PoolBg)
            .border(1.dp, PoolCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f + robotGlide
            val cy = h * 0.62f

            // Floating power tether cord
            val cordPath = Path().apply {
                moveTo(cx - 8f, cy - 8f)
                quadraticTo(cx - 18f, cy - 22f, w * 0.15f, 4f)
            }
            drawPath(
                path = cordPath,
                color = PoolCyan.copy(alpha = 0.5f),
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )

            // Rising water jet bubbles from top vent
            for (b in 0..2) {
                val prog = (bubblePhase + b * 0.33f) % 1f
                val by = (cy - 10f) - prog * 18f
                val bx = cx + sin(prog * 5f + b) * 4f
                drawCircle(
                    color = PoolCyan.copy(alpha = 1f - prog * 0.7f),
                    radius = 2.2f,
                    center = Offset(bx, by)
                )
            }

            // Pool Robot Body Dome
            drawRoundRect(
                color = PoolBlue,
                topLeft = Offset(cx - 15f, cy - 10f),
                size = Size(30f, 14f),
                cornerRadius = CornerRadius(7f, 7f)
            )
            // Top Accent Vent
            drawRoundRect(
                color = PoolSand,
                topLeft = Offset(cx - 7f, cy - 12f),
                size = Size(14f, 4f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            // Robot Tracks / Wheels
            drawCircle(color = PoolText, radius = 5.5f, center = Offset(cx - 10f, cy + 4f))
            drawCircle(color = PoolBg, radius = 2.5f, center = Offset(cx - 10f, cy + 4f))
            drawCircle(color = PoolText, radius = 5.5f, center = Offset(cx + 10f, cy + 4f))
            drawCircle(color = PoolBg, radius = 2.5f, center = Offset(cx + 10f, cy + 4f))
        }
    }
}

/**
 * Compact Coastal Header Banner for Reminders, Chem Locker & Log tabs.
 */
@Composable
fun AnimatedGraffitiCrewBanner(
    title: String,
    subtitle: String,
    badgeText: String,
    accentColor: Color = PoolCyan
) {
    val infinite = rememberInfiniteTransition(label = "coastal_banner")
    val drift by infinite.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner_drift"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(PoolHeroStart, PoolCard)
                )
            )
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mini Pool Float / Robot Icon Box
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PoolBg)
                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width * 0.5f
                val cy = size.height * 0.5f + drift
                drawCircle(color = Color.White, radius = 11f, center = Offset(cx, cy))
                for (a in listOf(0f, 90f, 180f, 270f)) {
                    drawArc(
                        color = PoolBlue,
                        startAngle = a,
                        sweepAngle = 45f,
                        useCenter = true,
                        topLeft = Offset(cx - 11f, cy - 11f),
                        size = Size(22f, 22f)
                    )
                }
                drawCircle(color = PoolBg, radius = 4.5f, center = Offset(cx, cy))
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PoolText
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = PoolMuted
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(PoolAccent)
                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = badgeText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PoolSand
            )
        }
    }
}
