package com.example.poolpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.poolpulse.model.PoolCalculations
import com.example.poolpulse.model.SeasonMode
import com.example.poolpulse.model.VitalConfig
import com.example.poolpulse.model.VolumeShape
import com.example.poolpulse.ui.theme.PoolAccent
import com.example.poolpulse.ui.theme.PoolBg
import com.example.poolpulse.ui.theme.PoolBorder
import com.example.poolpulse.ui.theme.PoolCard
import com.example.poolpulse.ui.theme.PoolCyan
import com.example.poolpulse.ui.theme.PoolGreen
import com.example.poolpulse.ui.theme.PoolMuted
import com.example.poolpulse.ui.theme.PoolOrange
import com.example.poolpulse.ui.theme.PoolRed
import com.example.poolpulse.ui.theme.PoolText
import com.example.poolpulse.viewmodel.PoolPulseUiState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun EditRangeDialog(
    vital: VitalConfig,
    onDismiss: () -> Unit,
    onSave: (Double?, Double?, Double?) -> Unit
) {
    var minText by remember(vital) { mutableStateOf(vital.idealMin.toString()) }
    var maxText by remember(vital) { mutableStateOf(vital.idealMax.toString()) }
    var targetText by remember(vital) { mutableStateOf(vital.target.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PoolCard)
                .border(1.dp, PoolCyan, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Edit ${vital.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PoolCyan
            )

            Text("Ideal Minimum", fontSize = 11.sp, color = PoolMuted)
            PoolModalTextField(
                value = minText,
                onValueChange = { minText = it },
                keyboardType = KeyboardType.Decimal,
                testTag = "range_min_input"
            )

            Text("Ideal Maximum", fontSize = 11.sp, color = PoolMuted)
            PoolModalTextField(
                value = maxText,
                onValueChange = { maxText = it },
                keyboardType = KeyboardType.Decimal,
                testTag = "range_max_input"
            )

            Text(
                text = "Target Level (▲ Orange Marker)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PoolOrange
            )
            PoolModalTextField(
                value = targetText,
                onValueChange = { targetText = it },
                keyboardType = KeyboardType.Decimal,
                testTag = "range_target_input"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PoolAccent,
                        contentColor = PoolText
                    ),
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        onSave(
                            minText.toDoubleOrNull(),
                            maxText.toDoubleOrNull(),
                            targetText.toDoubleOrNull()
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PoolCyan,
                        contentColor = PoolBg
                    ),
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_range_button")
                ) {
                    Text("Save Target", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun RestockDialog(
    state: PoolPulseUiState,
    onBrandSearch: (String) -> Unit,
    onUpdateFields: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var slotMenuExpanded by remember { mutableStateOf(false) }
    val slotOptions = listOf(
        "fc" to "Pool Essentials 10% Liquid Chlorine (fl oz)",
        "ph" to "Clorox pH Down 93.2% Dry Acid (lbs)",
        "alk" to "Arm & Hammer Baking Soda 100% (lbs)",
        "cya" to "Clorox Stabilizer 100% CYA (lbs)"
    )
    val currentSlotLabel = slotOptions.find { it.first == state.selectedRestockSlot }?.second
        ?: slotOptions.first().second

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PoolCard)
                .border(1.dp, PoolCyan, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Brand Lookup & Barcode Scan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PoolCyan
            )

            PoolModalTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onBrandSearch(it)
                },
                placeholder = "Type brand or UPC (e.g. CLOROX PH REDUCER)...",
                testTag = "brand_search_input"
            )

            if (state.brandMatchMessage.isNotBlank()) {
                Text(
                    text = state.brandMatchMessage,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PoolGreen
                )
            }

            Box {
                Surface(
                    color = PoolBg,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PoolBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { slotMenuExpanded = true }
                ) {
                    Text(
                        text = "$currentSlotLabel ▾",
                        fontSize = 12.sp,
                        color = PoolText,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                DropdownMenu(
                    expanded = slotMenuExpanded,
                    onDismissRequest = { slotMenuExpanded = false },
                    modifier = Modifier.background(PoolCard)
                ) {
                    slotOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label, color = PoolText, fontSize = 12.sp) },
                            onClick = {
                                onUpdateFields(
                                    key,
                                    state.restockQtyText,
                                    state.restockCostText,
                                    state.restockStoreText
                                )
                                slotMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Text("Amount on Bottle/Bag", fontSize = 11.sp, color = PoolMuted)
            PoolModalTextField(
                value = state.restockQtyText,
                onValueChange = {
                    onUpdateFields(
                        state.selectedRestockSlot,
                        it,
                        state.restockCostText,
                        state.restockStoreText
                    )
                },
                keyboardType = KeyboardType.Decimal,
                testTag = "restock_qty_input"
            )

            Text("Price Paid ($) & Store", fontSize = 11.sp, color = PoolMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    PoolModalTextField(
                        value = state.restockCostText,
                        onValueChange = {
                            onUpdateFields(
                                state.selectedRestockSlot,
                                state.restockQtyText,
                                it,
                                state.restockStoreText
                            )
                        },
                        keyboardType = KeyboardType.Decimal,
                        testTag = "restock_cost_input"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    PoolModalTextField(
                        value = state.restockStoreText,
                        onValueChange = {
                            onUpdateFields(
                                state.selectedRestockSlot,
                                state.restockQtyText,
                                state.restockCostText,
                                it
                            )
                        },
                        testTag = "restock_store_input"
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PoolAccent,
                        contentColor = PoolText
                    ),
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PoolCyan,
                        contentColor = PoolBg
                    ),
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_restock_button")
                ) {
                    Text("Save Receipt", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun SeasonGuideDialog(
    mode: SeasonMode,
    volumeGallons: Int,
    onSetNotificationsPaused: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val scale = volumeGallons / 7750.0
    val title = if (mode == SeasonMode.OPEN) {
        String.format(Locale.US, "☀️ Spring Pool Opening (%,d Gal)", volumeGallons)
    } else {
        String.format(Locale.US, "❄️ Winter Pool Closing (%,d Gal)", volumeGallons)
    }

    val steps = if (mode == SeasonMode.OPEN) {
        listOf(
            "1. Remove cover, reconnect pump/filter, and fill water to mid-skimmer.",
            "2. Backwash/rinse filter media and start pump.",
            "3. Test CYA & pH, then add ${(99.0 * scale).roundToInt()} fl oz Liquid Chlorine (10%).",
            "4. Brush walls & vacuum after 24 hrs."
        )
    } else {
        listOf(
            "1. Balance pH to 7.4–7.6, TA to 80–100 ppm, and CSI near 0.00.",
            "2. Shock with ${(79.0 * scale).roundToInt()} fl oz Liquid Chlorine.",
            String.format(Locale.US, "3. Add %.1f fl oz Polyquat 60 Algaecide.", 12.4 * scale),
            "4. Lower water below skimmer, drain equipment plugs, and cover."
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PoolCard)
                .border(1.dp, PoolCyan, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PoolCyan
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                steps.forEach { step ->
                    Text(step, fontSize = 12.sp, color = PoolText)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PoolBg.copy(alpha = 0.6f))
                    .border(1.dp, PoolBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (mode == SeasonMode.OPEN) {
                    Button(
                        onClick = { onSetNotificationsPaused(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PoolGreen,
                            contentColor = PoolBg
                        ),
                        shape = RoundedCornerShape(9.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔔 Resume Until Pool Closes", fontWeight = FontWeight.Black)
                    }
                    Text(
                        text = "Or Pause Notifications Until Pool Opens",
                        fontSize = 11.sp,
                        color = PoolRed,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onSetNotificationsPaused(true) }
                    )
                } else {
                    Button(
                        onClick = { onSetNotificationsPaused(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PoolRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(9.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔕 Pause Notifications Until Pool Opens", fontWeight = FontWeight.Black)
                    }
                    Text(
                        text = "Or Resume Until Pool Closes",
                        fontSize = 11.sp,
                        color = PoolGreen,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onSetNotificationsPaused(false) }
                    )
                }
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PoolCyan,
                    contentColor = PoolBg
                ),
                shape = RoundedCornerShape(9.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close Window", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun VolumeCalculatorDialog(
    initialVolume: Int,
    onDismiss: () -> Unit,
    onSaveVolume: (Int) -> Unit
) {
    var shape by remember { mutableStateOf(VolumeShape.DIRECT) }
    var shapeMenuExpanded by remember { mutableStateOf(false) }
    var directGallonsText by remember { mutableStateOf(initialVolume.toString()) }
    var lengthText by remember { mutableStateOf("20") }
    var widthText by remember { mutableStateOf("12") }
    var depthText by remember { mutableStateOf("3.3") }

    val calculatedGallons = PoolCalculations.calculateVolumeGallons(
        shape = shape,
        directGallons = directGallonsText.toIntOrNull() ?: initialVolume,
        lengthFt = lengthText.toDoubleOrNull() ?: 20.0,
        widthFt = widthText.toDoubleOrNull() ?: 12.0,
        depthFt = depthText.toDoubleOrNull() ?: 3.3
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PoolCard)
                .border(1.dp, PoolCyan, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Pool Volume Calculator",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PoolCyan
            )

            Box {
                Surface(
                    color = PoolBg,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PoolBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { shapeMenuExpanded = true }
                ) {
                    Text(
                        text = "${shape.label} ▾",
                        fontSize = 12.sp,
                        color = PoolText,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                DropdownMenu(
                    expanded = shapeMenuExpanded,
                    onDismissRequest = { shapeMenuExpanded = false },
                    modifier = Modifier.background(PoolCard)
                ) {
                    VolumeShape.entries.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.label, color = PoolText, fontSize = 12.sp) },
                            onClick = {
                                shape = item
                                shapeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            if (shape == VolumeShape.DIRECT) {
                Text("Gallons", fontSize = 11.sp, color = PoolMuted)
                PoolModalTextField(
                    value = directGallonsText,
                    onValueChange = { directGallonsText = it },
                    keyboardType = KeyboardType.Number,
                    testTag = "volume_gallons_input"
                )
            } else {
                Text("Diameter / Length (ft)", fontSize = 11.sp, color = PoolMuted)
                PoolModalTextField(
                    value = lengthText,
                    onValueChange = { lengthText = it },
                    keyboardType = KeyboardType.Decimal,
                    testTag = "volume_length_input"
                )
                if (shape == VolumeShape.RECT) {
                    Text("Width (ft)", fontSize = 11.sp, color = PoolMuted)
                    PoolModalTextField(
                        value = widthText,
                        onValueChange = { widthText = it },
                        keyboardType = KeyboardType.Decimal,
                        testTag = "volume_width_input"
                    )
                }
                Text("Avg Depth (ft)", fontSize = 11.sp, color = PoolMuted)
                PoolModalTextField(
                    value = depthText,
                    onValueChange = { depthText = it },
                    keyboardType = KeyboardType.Decimal,
                    testTag = "volume_depth_input"
                )
                Text(
                    text = String.format(Locale.US, "Calculated: %,d Gal", calculatedGallons),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PoolCyan
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PoolAccent,
                        contentColor = PoolText
                    ),
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onSaveVolume(calculatedGallons) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PoolCyan,
                        contentColor = PoolBg
                    ),
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_volume_button")
                ) {
                    Text("Save Volume", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int) -> Unit
) {
    var nameText by remember { mutableStateOf("Vacuum Pool") }
    var timeText by remember { mutableStateOf("5:00 PM") }
    var daysText by remember { mutableStateOf("2") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PoolCard)
                .border(1.dp, PoolCyan, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Add Custom Reminder",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PoolCyan
            )

            Text("Reminder Task Name", fontSize = 11.sp, color = PoolMuted)
            PoolModalTextField(
                value = nameText,
                onValueChange = { nameText = it },
                testTag = "reminder_name_input"
            )

            Text("Time (e.g. 5:00 PM)", fontSize = 11.sp, color = PoolMuted)
            PoolModalTextField(
                value = timeText,
                onValueChange = { timeText = it },
                testTag = "reminder_time_input"
            )

            Text("Repeat every X days", fontSize = 11.sp, color = PoolMuted)
            PoolModalTextField(
                value = daysText,
                onValueChange = { daysText = it },
                keyboardType = KeyboardType.Number,
                testTag = "reminder_days_input"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PoolAccent,
                        contentColor = PoolText
                    ),
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        onAdd(nameText, timeText, daysText.toIntOrNull() ?: 1)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PoolCyan,
                        contentColor = PoolBg
                    ),
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_reminder_button")
                ) {
                    Text("Add Reminder", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun PoolModalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, fontSize = 12.sp, color = PoolMuted) }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PoolBg,
            unfocusedContainerColor = PoolBg,
            focusedBorderColor = PoolCyan,
            unfocusedBorderColor = PoolBorder,
            focusedTextColor = PoolText,
            unfocusedTextColor = PoolText
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
