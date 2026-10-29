package com.example.poolpulse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poolpulse.ui.components.AnimatedGraffitiCrewBanner
import com.example.poolpulse.ui.theme.PoolBg
import com.example.poolpulse.ui.theme.PoolBorder
import com.example.poolpulse.ui.theme.PoolCard
import com.example.poolpulse.ui.theme.PoolCyan
import com.example.poolpulse.ui.theme.PoolGreen
import com.example.poolpulse.ui.theme.PoolMuted
import com.example.poolpulse.ui.theme.PoolSand
import com.example.poolpulse.ui.theme.PoolText
import com.example.poolpulse.viewmodel.PoolPulseUiState
import java.util.Locale

@Composable
fun RemindersTab(
    state: PoolPulseUiState,
    onMarkTaskDone: (Int) -> Unit,
    onOpenAddTaskModal: () -> Unit
) {
    val doneCount = state.tasks.count { it.completed }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedGraffitiCrewBanner(
            title = "Routine Pool Care Checklist",
            subtitle = "Consistent weekly skimming, brushing & testing keeps water clear.",
            badgeText = "$doneCount/${state.tasks.size} Done",
            accentColor = PoolCyan
        )

        SectionHeader(title = "TESTING & MAINTENANCE REMINDERS")

        state.tasks.forEach { task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PoolCard)
                    .border(
                        1.dp,
                        if (task.completed) PoolGreen.copy(alpha = 0.55f) else PoolBorder,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.name,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PoolText
                    )
                    Text(
                        text = "Every ${task.repeatDays} day(s) @ ${task.time}",
                        fontSize = 11.sp,
                        color = PoolMuted
                    )
                }
                Button(
                    onClick = { onMarkTaskDone(task.id) },
                    enabled = !task.completed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.completed) PoolGreen else PoolCyan,
                        contentColor = PoolBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                    modifier = Modifier.testTag("mark_task_done_${task.id}")
                ) {
                    Text(
                        text = if (task.completed) "Done ✓" else "Mark Done",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Button(
            onClick = onOpenAddTaskModal,
            colors = ButtonDefaults.buttonColors(
                containerColor = PoolCyan,
                contentColor = PoolBg
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_custom_reminder_button")
        ) {
            Text(
                text = "+ Add Custom Reminder",
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
fun ChemLockerTab(
    state: PoolPulseUiState,
    onOpenRestockModal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedGraffitiCrewBanner(
            title = "Chemical Inventory Locker",
            subtitle = "Track on-hand supplies, scan UPC barcodes & auto-deduct doses.",
            badgeText = "${state.inventory.size} Items",
            accentColor = PoolSand
        )

        // Stats summary card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PoolCard)
                .border(1.dp, PoolBorder, RoundedCornerShape(14.dp))
                .padding(vertical = 12.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn(
                label = "SPEND",
                value = String.format(Locale.US, "$%.2f", state.stats.spend)
            )
            StatColumn(
                label = "DOSES",
                value = state.stats.doses.toString()
            )
            StatColumn(
                label = "USED VAL",
                value = String.format(Locale.US, "$%.2f", state.stats.usedVal)
            )
        }

        Button(
            onClick = onOpenRestockModal,
            colors = ButtonDefaults.buttonColors(
                containerColor = PoolCyan,
                contentColor = PoolBg
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("open_restock_modal_button")
        ) {
            Text(
                text = "🔍 Brand Search / 📷 Scan Barcode",
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        state.inventory.values.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PoolCard)
                    .border(1.dp, PoolBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.name,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PoolText
                    )
                    Text(
                        text = item.chemical,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PoolCyan
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "$%.3f/%s • %s",
                            item.unitPrice,
                            item.unit,
                            item.store
                        ),
                        fontSize = 10.sp,
                        color = PoolMuted
                    )
                }
                Text(
                    text = "${item.quantity} ${item.unit}",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PoolSand
                )
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = PoolMuted,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PoolCyan,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CostLogTab(state: PoolPulseUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedGraffitiCrewBanner(
            title = "Dose History & Receipts",
            subtitle = "Complete log of chemical adjustments and store purchases.",
            badgeText = "${state.logs.size} Entries",
            accentColor = PoolCyan
        )

        SectionHeader(title = "DOSE HISTORY & RECEIPTS")

        state.logs.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PoolCard)
                    .border(1.dp, PoolBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PoolText
                    )
                    Text(
                        text = entry.subtitle,
                        fontSize = 10.5.sp,
                        color = PoolMuted
                    )
                }
                Text(
                    text = entry.amountBadge,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PoolSand
                )
            }
        }
    }
}
