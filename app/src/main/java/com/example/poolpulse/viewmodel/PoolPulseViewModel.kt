package com.example.poolpulse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poolpulse.model.ChemicalInventoryItem
import com.example.poolpulse.model.DoseLogEntry
import com.example.poolpulse.model.PoolCalculations
import com.example.poolpulse.model.PoolStats
import com.example.poolpulse.model.PoolTab
import com.example.poolpulse.model.PrescriptionItem
import com.example.poolpulse.model.ReminderTask
import com.example.poolpulse.model.SeasonMode
import com.example.poolpulse.model.VitalConfig
import com.example.poolpulse.model.WeatherDay
import com.example.poolpulse.model.WeatherMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

data class PoolPulseUiState(
    val currentTab: PoolTab = PoolTab.DASH,
    val volumeGallons: Int = 7750,
    val weatherMode: WeatherMode = WeatherMode.LIVE,
    val liveWeatherDays: List<WeatherDay>? = null,
    val vitals: List<VitalConfig> = defaultVitals(),
    val inventory: Map<String, ChemicalInventoryItem> = defaultInventory(),
    val tasks: List<ReminderTask> = defaultTasks(),
    val logs: List<DoseLogEntry> = defaultLogs(),
    val stats: PoolStats = PoolStats(),
    val notificationsPaused: Boolean = false,
    val poolDocReply: String = "Test your vitals and apply any Prescriptions above first. Tap a symptom chip if issues persist.",
    val editingVital: VitalConfig? = null,
    val showRestockModal: Boolean = false,
    val brandMatchMessage: String = "",
    val selectedRestockSlot: String = "fc",
    val restockQtyText: String = "5",
    val restockCostText: String = "14.98",
    val restockStoreText: String = "Walmart",
    val activeSeasonModal: SeasonMode? = null,
    val showVolumeModal: Boolean = false,
    val showAddTaskModal: Boolean = false
) {
    val forecastDays: List<WeatherDay>
        get() = when (weatherMode) {
            WeatherMode.RAIN -> listOf(
                WeatherDay("Today", "⛅", "68°F", "15%"),
                WeatherDay("Tomorrow", "⛈️", "64°F", "85%"),
                WeatherDay("Day 3", "☀️", "71°F", "10%")
            )
            WeatherMode.HEAT -> listOf(
                WeatherDay("Today", "☀️", "89°F", "5%"),
                WeatherDay("Tomorrow", "🔥", "94°F", "0%"),
                WeatherDay("Day 3", "☀️", "91°F", "10%")
            )
            WeatherMode.LIVE -> liveWeatherDays ?: listOf(
                WeatherDay("Today", "⛅", "69°F", "10%"),
                WeatherDay("Tomorrow", "☀️", "72°F", "15%"),
                WeatherDay("Day 3", "☀️", "74°F", "10%")
            )
        }

    val weatherBuffer: Double
        get() = if (weatherMode == WeatherMode.LIVE) 0.0 else 1.5

    val advisorText: String
        get() = if (weatherBuffer > 0.0) {
            val event = if (weatherMode == WeatherMode.RAIN) "rain storm" else "94°F UV heatwave"
            "Proactive +1.5 ppm FC buffer active for tomorrow's $event."
        } else {
            "Live local forecast steady. Standard chlorine target active."
        }

    val csiValue: Double
        get() {
            val byId = vitals.associateBy { it.id }
            return PoolCalculations.calculateCsi(
                ph = byId["pH"]?.value ?: 7.5,
                alk = byId["alk"]?.value ?: 80.0,
                cya = byId["cya"]?.value ?: 30.0,
                ch = byId["ch"]?.value ?: 200.0,
                tempF = byId["temp"]?.value ?: 78.0
            )
        }

    val waterHealthScore: Int
        get() = PoolCalculations.calculateWaterHealthScore(vitals, csiValue)

    val prescriptions: List<PrescriptionItem>
        get() = PoolCalculations.computePrescriptions(volumeGallons, vitals, inventory)
}

fun defaultVitals(): List<VitalConfig> = listOf(
    VitalConfig("fc", "Free Chlorine (FC)", "ppm", 4.0, 0.0, 15.0, 2.3, 6.0, 3.5, false),
    VitalConfig("pH", "pH Level", "pH", 7.5, 6.8, 8.2, 7.4, 7.6, 7.5, false),
    VitalConfig("alk", "Total Alkalinity (TA)", "ppm", 80.0, 40.0, 200.0, 70.0, 90.0, 80.0, false),
    VitalConfig("cya", "Stabilizer (CYA)", "ppm", 30.0, 0.0, 100.0, 30.0, 50.0, 40.0, false),
    VitalConfig("ch", "Calcium Hardness (CH)", "ppm", 200.0, 50.0, 450.0, 150.0, 250.0, 200.0, false),
    VitalConfig("temp", "Water Temperature", "°F", 78.0, 40.0, 95.0, 76.0, 86.0, 82.0, false)
)

fun defaultInventory(): Map<String, ChemicalInventoryItem> = linkedMapOf(
    "fc" to ChemicalInventoryItem(
        key = "fc",
        name = "Pool Essentials Liquid Chlorine",
        chemical = "10% Sodium Hypochlorite",
        quantity = 256.0,
        unit = "fl oz",
        unitPrice = 0.045,
        store = "Walmart"
    ),
    "ph" to ChemicalInventoryItem(
        key = "ph",
        name = "Clorox Pool&Spa pH Down",
        chemical = "93.2% Sodium Bisulfate",
        quantity = 5.0,
        unit = "lbs",
        unitPrice = 3.00,
        store = "Lowe's"
    ),
    "alk" to ChemicalInventoryItem(
        key = "alk",
        name = "Arm & Hammer Baking Soda",
        chemical = "100% Sodium Bicarbonate",
        quantity = 5.0,
        unit = "lbs",
        unitPrice = 1.50,
        store = "Walmart"
    ),
    "cya" to ChemicalInventoryItem(
        key = "cya",
        name = "Clorox Chlorine Stabilizer",
        chemical = "100% Cyanuric Acid",
        quantity = 4.0,
        unit = "lbs",
        unitPrice = 4.99,
        store = "Walmart"
    )
)

fun defaultTasks(): List<ReminderTask> = listOf(
    ReminderTask(1, "Test Free Chlorine & pH", "5:00 PM", 1),
    ReminderTask(2, "Vacuum Pool & Brush Walls", "10:00 AM", 2)
)

fun defaultLogs(): List<DoseLogEntry> = listOf(
    DoseLogEntry(1, "Pool Pulse Initialized", "7,750 Gal Profile Active", "Ready")
)

class PoolPulseViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(recomputeAutoTargets(PoolPulseUiState()))
    val uiState: StateFlow<PoolPulseUiState> = _uiState.asStateFlow()

    init {
        fetchLiveWeather()
    }

    private fun recomputeAutoTargets(state: PoolPulseUiState): PoolPulseUiState {
        val cyaVal = state.vitals.find { it.id == "cya" }?.value ?: 30.0
        val buf = if (state.weatherMode == WeatherMode.LIVE) 0.0 else 1.5
        val updatedVitals = state.vitals.map { vital ->
            if (vital.id == "fc" && !vital.custom) {
                val effCya = max(20.0, cyaVal)
                val iMin = ((effCya * 0.075 * 10.0).roundToInt()) / 10.0
                val targ = (((effCya * 0.115 + buf) * 10.0).roundToInt()) / 10.0
                val iMax = (((targ + 2.5) * 10.0).roundToInt()) / 10.0
                vital.copy(idealMin = iMin, idealMax = iMax, target = targ)
            } else {
                vital
            }
        }
        return state.copy(vitals = updatedVitals)
    }

    fun selectTab(tab: PoolTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setWeatherMode(mode: WeatherMode) {
        _uiState.update { recomputeAutoTargets(it.copy(weatherMode = mode)) }
    }

    /**
     * Updates a vital reading without prematurely clamping to min..max while the user is typing
     * multi-digit numbers (such as typing "80" for Water Temperature or "45" for CYA).
     */
    fun updateVitalValue(id: String, newValue: Double) {
        _uiState.update { current ->
            val updated = current.vitals.map { vital ->
                if (vital.id == id) {
                    val rounded = ((newValue * 10.0).roundToInt()) / 10.0
                    vital.copy(value = rounded)
                } else vital
            }
            recomputeAutoTargets(current.copy(vitals = updated))
        }
    }

    fun openRangeEdit(id: String) {
        val item = _uiState.value.vitals.find { it.id == id }
        _uiState.update { it.copy(editingVital = item) }
    }

    fun closeRangeEdit() {
        _uiState.update { it.copy(editingVital = null) }
    }

    fun saveRangeEdit(id: String, minVal: Double?, maxVal: Double?, targetVal: Double?) {
        _uiState.update { current ->
            val updated = current.vitals.map { vital ->
                if (vital.id == id) {
                    vital.copy(
                        idealMin = minVal ?: vital.idealMin,
                        idealMax = maxVal ?: vital.idealMax,
                        target = targetVal ?: vital.target,
                        custom = true
                    )
                } else {
                    vital
                }
            }
            current.copy(vitals = updated, editingVital = null)
        }
    }

    fun applyDose(rx: PrescriptionItem) {
        _uiState.update { current ->
            val invItem = current.inventory[rx.inventoryKey] ?: return@update current
            val newQty = max(0.0, ((invItem.quantity - rx.quantity) * 100.0).roundToInt() / 100.0)
            val newInv = current.inventory.toMutableMap().apply {
                put(rx.inventoryKey, invItem.copy(quantity = newQty))
            }
            val newStats = current.stats.copy(
                doses = current.stats.doses + 1,
                usedVal = current.stats.usedVal + rx.estimatedCost
            )
            val newLog = DoseLogEntry(
                id = current.logs.size + 1,
                title = rx.title,
                subtitle = "Auto-deducted ${rx.quantity} ${invItem.unit}",
                amountBadge = String.format(Locale.US, "-$%.2f", rx.estimatedCost)
            )
            val targetId = if (rx.inventoryKey == "ph") "pH" else rx.inventoryKey
            val updatedVitals = current.vitals.map { vital ->
                if (vital.id == targetId) vital.copy(value = vital.target) else vital
            }
            recomputeAutoTargets(
                current.copy(
                    inventory = newInv,
                    stats = newStats,
                    logs = listOf(newLog) + current.logs,
                    vitals = updatedVitals
                )
            )
        }
    }

    fun askPoolDoc(query: String) {
        _uiState.update { current ->
            val byId = current.vitals.associateBy { it.id }
            val reply = PoolCalculations.generatePoolDocAnswer(
                query = query,
                fc = byId["fc"]?.value ?: 4.0,
                ph = byId["pH"]?.value ?: 7.5,
                cya = byId["cya"]?.value ?: 30.0,
                volumeGallons = current.volumeGallons
            )
            current.copy(poolDocReply = reply)
        }
    }

    fun openSeasonModal(mode: SeasonMode) {
        _uiState.update { it.copy(activeSeasonModal = mode) }
    }

    fun closeSeasonModal() {
        _uiState.update { it.copy(activeSeasonModal = null) }
    }

    fun setNotificationsPaused(paused: Boolean) {
        _uiState.update { it.copy(notificationsPaused = paused, activeSeasonModal = null) }
    }

    fun openVolumeModal() {
        _uiState.update { it.copy(showVolumeModal = true) }
    }

    fun closeVolumeModal() {
        _uiState.update { it.copy(showVolumeModal = false) }
    }

    fun saveVolume(gallons: Int) {
        _uiState.update { current ->
            recomputeAutoTargets(
                current.copy(
                    volumeGallons = if (gallons > 0) gallons else 7750,
                    showVolumeModal = false
                )
            )
        }
    }

    fun openRestockModal() {
        _uiState.update {
            it.copy(
                showRestockModal = true,
                brandMatchMessage = "",
                selectedRestockSlot = "fc",
                restockQtyText = "5",
                restockCostText = "14.98",
                restockStoreText = "Walmart"
            )
        }
    }

    fun closeRestockModal() {
        _uiState.update { it.copy(showRestockModal = false) }
    }

    fun onBrandSearchInput(query: String) {
        val s = query.lowercase(Locale.US)
        _uiState.update { current ->
            when {
                s.contains("colorx") || s.contains("clorox") || s.contains("ph") ->
                    current.copy(
                        selectedRestockSlot = "ph",
                        restockQtyText = "5",
                        brandMatchMessage = "✓ Matched: Clorox pH Down (93.2% Sodium Bisulfate • lbs)"
                    )
                s.contains("essential") || s.contains("liq") || s.contains("bleach") ->
                    current.copy(
                        selectedRestockSlot = "fc",
                        restockQtyText = "128",
                        brandMatchMessage = "✓ Matched: Pool Essentials 10% Liquid Chlorine (128 fl oz)"
                    )
                s.contains("arm") || s.contains("hammer") || s.contains("baking") ->
                    current.copy(
                        selectedRestockSlot = "alk",
                        restockQtyText = "5",
                        brandMatchMessage = "✓ Matched: Arm & Hammer Baking Soda (100% • lbs)"
                    )
                s.contains("stab") || s.contains("cya") ->
                    current.copy(
                        selectedRestockSlot = "cya",
                        restockQtyText = "4",
                        brandMatchMessage = "✓ Matched: Clorox Stabilizer (100% CYA • lbs)"
                    )
                else -> current.copy(brandMatchMessage = "")
            }
        }
        val digitsOnly = query.trim()
        if (digitsOnly.length in 8..14 && digitsOnly.all { it.isDigit() }) {
            lookupUpc(digitsOnly)
        }
    }

    fun updateRestockFields(
        slot: String = _uiState.value.selectedRestockSlot,
        qty: String = _uiState.value.restockQtyText,
        cost: String = _uiState.value.restockCostText,
        store: String = _uiState.value.restockStoreText
    ) {
        _uiState.update {
            it.copy(
                selectedRestockSlot = slot,
                restockQtyText = qty,
                restockCostText = cost,
                restockStoreText = store
            )
        }
    }

    fun saveRestock() {
        _uiState.update { current ->
            val key = current.selectedRestockSlot
            val invItem = current.inventory[key] ?: return@update current
            val qty = current.restockQtyText.toDoubleOrNull()?.takeIf { it > 0.0 } ?: 1.0
            val cost = current.restockCostText.toDoubleOrNull()?.takeIf { it >= 0.0 } ?: 0.0
            val store = current.restockStoreText.ifBlank { "Store" }

            val updatedQty = ((invItem.quantity + qty) * 100.0).roundToInt() / 100.0
            val updatedUnitPrice = ((cost / qty) * 1000.0).roundToInt() / 1000.0
            val newInv = current.inventory.toMutableMap().apply {
                put(
                    key,
                    invItem.copy(
                        quantity = updatedQty,
                        unitPrice = updatedUnitPrice,
                        store = store
                    )
                )
            }
            val newStats = current.stats.copy(spend = current.stats.spend + cost)
            val newLog = DoseLogEntry(
                id = current.logs.size + 1,
                title = "Bought +$qty ${invItem.unit} ${invItem.name}",
                subtitle = "$store • ${invItem.chemical}",
                amountBadge = String.format(Locale.US, "$%.2f", cost)
            )
            recomputeAutoTargets(
                current.copy(
                    inventory = newInv,
                    stats = newStats,
                    logs = listOf(newLog) + current.logs,
                    showRestockModal = false
                )
            )
        }
    }

    fun markTaskDone(taskId: Int) {
        _uiState.update { current ->
            val updated = current.tasks.map { task ->
                if (task.id == taskId) task.copy(completed = true) else task
            }
            current.copy(tasks = updated)
        }
    }

    fun openAddTaskModal() {
        _uiState.update { it.copy(showAddTaskModal = true) }
    }

    fun closeAddTaskModal() {
        _uiState.update { it.copy(showAddTaskModal = false) }
    }

    fun addCustomReminder(name: String, time: String, repeatDays: Int) {
        if (name.isBlank()) return
        _uiState.update { current ->
            val newTask = ReminderTask(
                id = current.tasks.size + 1,
                name = name.trim(),
                time = time.ifBlank { "5:00 PM" },
                repeatDays = if (repeatDays > 0) repeatDays else 1
            )
            current.copy(
                tasks = current.tasks + newTask,
                showAddTaskModal = false
            )
        }
    }

    private fun fetchLiveWeather() {
        viewModelScope.launch {
            val days = withContext(Dispatchers.IO) {
                try {
                    val url = URL(
                        "https://api.open-meteo.com/v1/forecast?latitude=36.595&longitude=-82.188&daily=temperature_2m_max,precipitation_probability_max&temperature_unit=fahrenheit&timezone=auto&forecast_days=3"
                    )
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 5000
                        readTimeout = 5000
                        requestMethod = "GET"
                    }
                    if (conn.responseCode == 200) {
                        val body = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(body)
                        val daily = json.optJSONObject("daily")
                        if (daily != null) {
                            val temps = daily.getJSONArray("temperature_2m_max")
                            val precips = daily.getJSONArray("precipitation_probability_max")
                            val labels = listOf("Today", "Tomorrow", "Day 3")
                            (0..2).map { i ->
                                val p = precips.optInt(i, 10)
                                val t = temps.optDouble(i, 72.0).roundToInt()
                                WeatherDay(
                                    dayLabel = labels[i],
                                    icon = if (p > 50) "🌧️" else "☀️",
                                    tempLabel = "${t}°F",
                                    precipLabel = "${p}%"
                                )
                            }
                        } else null
                    } else null
                } catch (_: Exception) {
                    null
                }
            }
            if (days != null) {
                _uiState.update { it.copy(liveWeatherDays = days) }
            }
        }
    }

    fun lookupUpc(upc: String) {
        viewModelScope.launch {
            val productName = withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://world.openfoodfacts.org/api/v0/product/$upc.json")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 5000
                        readTimeout = 5000
                        requestMethod = "GET"
                    }
                    if (conn.responseCode == 200) {
                        val body = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(body)
                        json.optJSONObject("product")
                            ?.optString("product_name")
                            ?.takeIf { it.isNotBlank() }
                    } else null
                } catch (_: Exception) {
                    null
                }
            }
            if (productName != null) {
                _uiState.update {
                    it.copy(brandMatchMessage = "✓ Scanned: $productName")
                }
            }
        }
    }
}
