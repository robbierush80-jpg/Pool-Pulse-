package com.example.poolpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.poolpulse.model.PoolTab
import com.example.poolpulse.ui.components.AddReminderDialog
import com.example.poolpulse.ui.components.EditRangeDialog
import com.example.poolpulse.ui.components.RestockDialog
import com.example.poolpulse.ui.components.SeasonGuideDialog
import com.example.poolpulse.ui.components.VolumeCalculatorDialog
import com.example.poolpulse.ui.screens.ChemLockerTab
import com.example.poolpulse.ui.screens.CostLogTab
import com.example.poolpulse.ui.screens.DashboardTab
import com.example.poolpulse.ui.screens.RemindersTab
import com.example.poolpulse.ui.theme.PoolAccent
import com.example.poolpulse.ui.theme.PoolBg
import com.example.poolpulse.ui.theme.PoolBlue
import com.example.poolpulse.ui.theme.PoolBorder
import com.example.poolpulse.ui.theme.PoolCard
import com.example.poolpulse.ui.theme.PoolCyan
import com.example.poolpulse.ui.theme.PoolMuted
import com.example.poolpulse.ui.theme.PoolPulseTheme
import com.example.poolpulse.ui.theme.PoolSand
import com.example.poolpulse.ui.theme.PoolText
import com.example.poolpulse.viewmodel.PoolPulseViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PoolPulseTheme {
                PoolPulseApp()
            }
        }
    }
}

@Composable
fun PoolPulseApp(viewModel: PoolPulseViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.currentTab != PoolTab.DASH) {
        BackHandler {
            viewModel.selectTab(PoolTab.DASH)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PoolBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(modifier = Modifier.background(PoolCard)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(PoolCyan, PoolBlue)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "P",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = PoolBg
                            )
                        }
                        Column {
                            Text(
                                text = "Pool Pulse",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PoolText
                            )
                            Text(
                                text = "PROACTIVE POOL CARE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PoolSand
                            )
                        }
                    }

                    Surface(
                        color = PoolAccent,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PoolCyan.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .testTag("volume_pill_button")
                            .clickable { viewModel.openVolumeModal() }
                    ) {
                        Text(
                            text = String.format(Locale.US, "⚙️ %,d GAL", state.volumeGallons),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PoolSand,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                        )
                    }
                }
                HorizontalDivider(color = PoolBorder, thickness = 1.dp)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = PoolCyan.copy(alpha = 0.4f), thickness = 1.dp)
                NavigationBar(
                    containerColor = Color.Transparent,
                    contentColor = PoolText,
                    modifier = Modifier.drawBehind {
                        val w = size.width
                        val h = size.height

                        // 1. Shallow Turquoise Shoreline Water at top of nav bar
                        drawRect(color = Color(0xFF0E4D78))

                        val shallowWaterPath = Path().apply {
                            moveTo(0f, 0f)
                            var x = 0f
                            while (x <= w + 16f) {
                                val y = h * 0.28f + sin((x / w) * 3f * PI.toFloat()) * (h * 0.06f)
                                lineTo(x, y)
                                x += 12f
                            }
                            lineTo(w, 0f)
                            close()
                        }
                        drawPath(path = shallowWaterPath, color = Color(0xFF1976A5))

                        // 2. Cartoon Golden Beach Sand Dunes
                        val wetSandPath = Path().apply {
                            moveTo(0f, h)
                            var x = 0f
                            while (x <= w + 16f) {
                                val y = h * 0.30f + sin((x / w) * 3f * PI.toFloat()) * (h * 0.06f)
                                lineTo(x, y)
                                x += 12f
                            }
                            lineTo(w, h)
                            close()
                        }
                        drawPath(path = wetSandPath, color = Color(0xFFC49A5B))

                        // Soft White Shoreline Foam Ribbon where surf meets the beach
                        val shoreFoamPath = Path().apply {
                            var x = 0f
                            var first = true
                            while (x <= w + 16f) {
                                val y = h * 0.30f + sin((x / w) * 3f * PI.toFloat()) * (h * 0.06f)
                                if (first) {
                                    moveTo(x, y)
                                    first = false
                                } else {
                                    lineTo(x, y)
                                }
                                x += 12f
                            }
                        }
                        drawPath(
                            path = shoreFoamPath,
                            color = Color(0xFFE0F7FF),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Warm Dry Cartoon Sand Dune across lower 62% of bar
                        val drySandPath = Path().apply {
                            moveTo(0f, h)
                            var x = 0f
                            while (x <= w + 16f) {
                                val y = h * 0.44f + cos((x / w) * 2.2f * PI.toFloat() + 0.5f) * (h * 0.05f)
                                lineTo(x, y)
                                x += 12f
                            }
                            lineTo(w, h)
                            close()
                        }
                        drawPath(path = drySandPath, color = Color(0xFFD9B375))

                        // Subtle sand speckles & tiny cartoon coral starfish on the beach
                        val starCx = w * 0.25f
                        val starCy = h * 0.76f
                        for (arm in 0 until 5) {
                            val angle = (arm * 72f - 18f) * (PI.toFloat() / 180f)
                            drawLine(
                                color = Color(0xFFE05A47),
                                start = Offset(starCx, starCy),
                                end = Offset(
                                    starCx + cos(angle) * 6.dp.toPx(),
                                    starCy + sin(angle) * 6.dp.toPx()
                                ),
                                strokeWidth = 2.6.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                        // Tiny cartoon seashell on the right dune
                        val shellCx = w * 0.76f
                        val shellCy = h * 0.78f
                        drawCircle(
                            color = Color(0xFFFFF3E0),
                            radius = 4.5.dp.toPx(),
                            center = Offset(shellCx, shellCy)
                        )
                    }
                ) {
                    val items = listOf(
                        Triple(PoolTab.DASH, "Pulse Dash", Icons.Outlined.RadioButtonChecked),
                        Triple(PoolTab.TASKS, "Reminders", Icons.Outlined.CheckBox),
                        Triple(PoolTab.INV, "Chem Locker", Icons.Outlined.Inventory2),
                        Triple(PoolTab.LOGS, "Cost & Log", Icons.Outlined.ReceiptLong)
                    )
                    items.forEach { (tab, label, icon) ->
                        val selected = state.currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PoolCyan,
                                selectedTextColor = Color(0xFF071B2F),
                                unselectedIconColor = Color(0xFF0A2540),
                                unselectedTextColor = Color(0xFF0A2540),
                                indicatorColor = Color(0xFF0A2239)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(modifier = Modifier.widthIn(max = 600.dp)) {
                when (state.currentTab) {
                    PoolTab.DASH -> DashboardTab(
                        state = state,
                        onWeatherModeChange = viewModel::setWeatherMode,
                        onVitalValueChange = viewModel::updateVitalValue,
                        onOpenRangeEdit = viewModel::openRangeEdit,
                        onApplyDose = viewModel::applyDose,
                        onAskPoolDoc = viewModel::askPoolDoc,
                        onOpenSeason = viewModel::openSeasonModal
                    )
                    PoolTab.TASKS -> RemindersTab(
                        state = state,
                        onMarkTaskDone = viewModel::markTaskDone,
                        onOpenAddTaskModal = viewModel::openAddTaskModal
                    )
                    PoolTab.INV -> ChemLockerTab(
                        state = state,
                        onOpenRestockModal = viewModel::openRestockModal
                    )
                    PoolTab.LOGS -> CostLogTab(state = state)
                }
            }
        }

        // Modals
        state.editingVital?.let { vital ->
            EditRangeDialog(
                vital = vital,
                onDismiss = viewModel::closeRangeEdit,
                onSave = { minV, maxV, targV ->
                    viewModel.saveRangeEdit(vital.id, minV, maxV, targV)
                }
            )
        }

        if (state.showRestockModal) {
            RestockDialog(
                state = state,
                onBrandSearch = viewModel::onBrandSearchInput,
                onUpdateFields = viewModel::updateRestockFields,
                onDismiss = viewModel::closeRestockModal,
                onSave = viewModel::saveRestock
            )
        }

        state.activeSeasonModal?.let { mode ->
            SeasonGuideDialog(
                mode = mode,
                volumeGallons = state.volumeGallons,
                onSetNotificationsPaused = viewModel::setNotificationsPaused,
                onDismiss = viewModel::closeSeasonModal
            )
        }

        if (state.showVolumeModal) {
            VolumeCalculatorDialog(
                initialVolume = state.volumeGallons,
                onDismiss = viewModel::closeVolumeModal,
                onSaveVolume = viewModel::saveVolume
            )
        }

        if (state.showAddTaskModal) {
            AddReminderDialog(
                onDismiss = viewModel::closeAddTaskModal,
                onAdd = viewModel::addCustomReminder
            )
        }
    }
}
