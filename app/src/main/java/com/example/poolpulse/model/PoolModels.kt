package com.example.poolpulse.model

import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

enum class PoolTab {
    DASH, TASKS, INV, LOGS
}

enum class WeatherMode(val id: String, val label: String) {
    LIVE("live", "Live Weather"),
    RAIN("rain", "Incoming Rain Tomorrow"),
    HEAT("heat", "92°F UV Heatwave")
}

enum class VolumeShape(val id: String, val label: String) {
    DIRECT("dir", "Enter Gallons Directly"),
    ROUND("rnd", "Round Pool Calculator"),
    RECT("rct", "Rectangular / Oval Calculator")
}

enum class SeasonMode {
    OPEN, CLOSE
}

data class VitalConfig(
    val id: String,
    val name: String,
    val unit: String,
    val value: Double,
    val min: Double,
    val max: Double,
    val idealMin: Double,
    val idealMax: Double,
    val target: Double,
    val custom: Boolean = false
) {
    val swimUniRole: String
        get() = when (id) {
            "fc" -> "🛡️ Sanitizer • Zaps algae & bacteria"
            "pH" -> "👀 Comfort & Chlorine Power"
            "alk" -> "⚓ pH Buffer • Prevents wild pH swings"
            "cya" -> "🕶️ Chlorine Sunscreen • Blocks UV burn-off"
            "ch" -> "🧱 Surface Shield • Protects plaster & heaters"
            "temp" -> "🌡️ Swimmer Comfort & CSI Factor"
            else -> "💧 Water Vital"
        }

    val stepDelta: Double
        get() = when (id) {
            "fc" -> 0.5
            "pH" -> 0.1
            "alk" -> 10.0
            "cya" -> 5.0
            "ch" -> 10.0
            "temp" -> 1.0
            else -> 1.0
        }
}

data class ChemicalInventoryItem(
    val key: String,
    val name: String,
    val chemical: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val store: String
)

data class PrescriptionItem(
    val title: String,
    val description: String,
    val howToApply: String,
    val chemical: String,
    val inventoryKey: String,
    val quantity: Double,
    val unit: String,
    val estimatedCost: Double
)

data class ReminderTask(
    val id: Int,
    val name: String,
    val time: String,
    val repeatDays: Int,
    val completed: Boolean = false
)

data class DoseLogEntry(
    val id: Int,
    val title: String,
    val subtitle: String,
    val amountBadge: String
)

data class WeatherDay(
    val dayLabel: String,
    val icon: String,
    val tempLabel: String,
    val precipLabel: String
)

data class PoolStats(
    val spend: Double = 48.50,
    val doses: Int = 0,
    val usedVal: Double = 0.00
)

object PoolCalculations {

    fun formatDryOz(oz: Double): String {
        return if (oz < 16.0) {
            String.format(Locale.US, "%.1f dry oz", oz)
        } else {
            val lbs = oz / 16.0
            val wholeLbs = floor(lbs).toInt()
            val remOz = oz % 16.0
            String.format(Locale.US, "%.2f lbs (%d lb %.1f oz)", lbs, wholeLbs, remOz)
        }
    }

    fun calculateCsi(ph: Double, alk: Double, cya: Double, ch: Double, tempF: Double): Double {
        val effTa = max(10.0, alk - cya * 0.33)
        val tC = (tempF - 32.0) * 5.0 / 9.0
        val tempFactor = -13.12 * log10(tC + 273.0) + 34.55
        val chAlkFactor = (log10(max(10.0, ch)) - 0.4) + log10(effTa)
        val raw = ph - ((9.3 + tempFactor) - chAlkFactor)
        return (raw * 100.0).roundToInt() / 100.0
    }

    fun calculateWaterHealthScore(vitals: List<VitalConfig>, csi: Double): Int {
        if (vitals.isEmpty()) return 100
        var totalPoints = 0.0
        vitals.forEach { v ->
            if (v.value in v.idealMin..v.idealMax) {
                totalPoints += 15.0
            } else {
                val dist = if (v.value < v.idealMin) v.idealMin - v.value else v.value - v.idealMax
                val span = (v.max - v.min).coerceAtLeast(1.0)
                val penalty = (dist / span) * 30.0
                totalPoints += (15.0 - penalty).coerceIn(3.0, 12.0)
            }
        }
        val csiPoints = if (abs(csi) <= 0.30) 10.0 else (10.0 - abs(csi) * 10.0).coerceIn(2.0, 8.0)
        return (totalPoints + csiPoints).roundToInt().coerceIn(25, 100)
    }

    fun calculateVolumeGallons(
        shape: VolumeShape,
        directGallons: Int,
        lengthFt: Double,
        widthFt: Double,
        depthFt: Double
    ): Int {
        return when (shape) {
            VolumeShape.DIRECT -> if (directGallons > 0) directGallons else 7750
            VolumeShape.ROUND -> (Math.PI * (lengthFt / 2.0).pow(2.0) * depthFt * 7.48).roundToInt()
            VolumeShape.RECT -> (lengthFt * widthFt * depthFt * 7.48).roundToInt()
        }
    }

    fun computePrescriptions(
        volumeGallons: Int,
        vitals: List<VitalConfig>,
        inventory: Map<String, ChemicalInventoryItem>
    ): List<PrescriptionItem> {
        val scale = volumeGallons / 7750.0
        val byId = vitals.associateBy { it.id }
        val fc = byId["fc"] ?: return emptyList()
        val ph = byId["pH"] ?: return emptyList()
        val alk = byId["alk"] ?: return emptyList()
        val cya = byId["cya"] ?: return emptyList()

        val list = mutableListOf<PrescriptionItem>()

        val fcInv = inventory["fc"]
        if (fc.value < fc.target && fcInv != null) {
            val oz = (fc.target - fc.value) * 9.9 * scale
            val roundedQty = ((oz * 10.0).roundToInt()) / 10.0
            list.add(
                PrescriptionItem(
                    title = String.format(Locale.US, "Add %.1f fl oz %s", oz, fcInv.name),
                    description = String.format(
                        Locale.US,
                        "Raises FC from %.1f to %.1f ppm (%,d gal).",
                        fc.value,
                        fc.target,
                        volumeGallons
                    ),
                    howToApply = "Swim U Step: Pour slowly in front of a running return jet with the pump ON. Wait 30 mins before adding other chemicals.",
                    chemical = fcInv.chemical,
                    inventoryKey = "fc",
                    quantity = roundedQty,
                    unit = fcInv.unit,
                    estimatedCost = oz * fcInv.unitPrice
                )
            )
        }

        val phInv = inventory["ph"]
        if (ph.value > ph.idealMax && phInv != null) {
            val oz = ((ph.value - ph.target) / 0.2) * 8.5 * (alk.value / 90.0) * scale
            val lb = ((oz / 16.0 * 100.0).roundToInt()) / 100.0
            list.add(
                PrescriptionItem(
                    title = "Add ${formatDryOz(oz)} ${phInv.name}",
                    description = String.format(
                        Locale.US,
                        "Lowers pH from %.1f to %.1f.",
                        ph.value,
                        ph.target
                    ),
                    howToApply = "Swim U Step: Broadcast dry acid evenly over the deep end with the pump running, then brush any settled granules.",
                    chemical = phInv.chemical,
                    inventoryKey = "ph",
                    quantity = lb,
                    unit = phInv.unit,
                    estimatedCost = lb * phInv.unitPrice
                )
            )
        }

        val alkInv = inventory["alk"]
        if (alk.value < alk.idealMin && alkInv != null) {
            val oz = ((alk.target - alk.value) / 10.0) * 17.3 * scale
            val lb = ((oz / 16.0 * 100.0).roundToInt()) / 100.0
            list.add(
                PrescriptionItem(
                    title = "Add ${formatDryOz(oz)} ${alkInv.name}",
                    description = String.format(
                        Locale.US,
                        "Raises TA from %.0f to %.0f ppm.",
                        alk.value,
                        alk.target
                    ),
                    howToApply = "Swim U Step: Broadcast baking soda across the water surface with the pump running. Always balance Alkalinity before pH!",
                    chemical = alkInv.chemical,
                    inventoryKey = "alk",
                    quantity = lb,
                    unit = alkInv.unit,
                    estimatedCost = lb * alkInv.unitPrice
                )
            )
        }

        val cyaInv = inventory["cya"]
        if (cya.value < cya.idealMin && cyaInv != null) {
            val oz = ((cya.target - cya.value) / 10.0) * 10.3 * scale
            val lb = ((oz / 16.0 * 100.0).roundToInt()) / 100.0
            list.add(
                PrescriptionItem(
                    title = "Add ${formatDryOz(oz)} ${cyaInv.name}",
                    description = String.format(
                        Locale.US,
                        "Raises CYA from %.0f to %.0f ppm.",
                        cya.value,
                        cya.target
                    ),
                    howToApply = "Swim U Step: Place stabilizer granules inside a skimmer sock in the basket or hang in front of a return jet; squeeze occasionally to dissolve.",
                    chemical = cyaInv.chemical,
                    inventoryKey = "cya",
                    quantity = lb,
                    unit = cyaInv.unit,
                    estimatedCost = lb * cyaInv.unitPrice
                )
            )
        }

        return list
    }

    fun generatePoolDocAnswer(
        query: String,
        fc: Double,
        ph: Double,
        cya: Double,
        volumeGallons: Int
    ): String {
        val s = query.lowercase(Locale.US)
        val scale = volumeGallons / 7750.0
        val slamFc = (cya * 0.4).roundToInt()
        val slamOz = max(0.0, (cya * 0.4 - fc) * 9.9 * scale).roundToInt()
        val algaeOz = max(16.0, (cya * 0.4 - fc) * 9.9 * scale).roundToInt()
        val foamOz = (30.0 * scale).roundToInt()

        return when {
            s.contains("cloud") ->
                "☁️ Cloudy Water Gameplan: 1) Dial pH to 7.4. 2) Boost FC to $slamFc ppm SLAM level ($slamOz fl oz Liquid Chlorine). 3) Run filter 24 hrs & backwash!"
            s.contains("alg") || s.contains("green") ->
                "🦠 Green Algae Takedown: 1) Brush all walls & steps hard. 2) Shock to $slamFc ppm FC ($algaeOz fl oz Liquid Chlorine). 3) Run pump 24/7 & clean filter daily!"
            s.contains("foam") ->
                "🫧 Foamy Water Fix: Caused by cheap soaps/oils or quaternary algaecide. Oxidize organics with $foamOz fl oz Liquid Chlorine and only use non-foaming Polyquat 60."
            s.contains("burn") || s.contains("eye") ->
                String.format(
                    Locale.US,
                    "👀 Burning Eyes Truth: It's NOT too much chlorine—it's chloramines or pH out of bounds (yours is %.1f)! Balance pH to 7.5 and boost FC +3 ppm.",
                    ph
                )
            s.contains("shock") || s.contains("slam") ->
                "⚡ Swim U Shock Protocol: Wait until sundown so UV doesn't eat your chlorine! Add $algaeOz fl oz Liquid Chlorine (10%) to reach $slamFc ppm FC."
            else ->
                String.format(
                    Locale.US,
                    "Yo! Based on your live vitals (FC: %.1f ppm, pH: %.1f), knock out any Smart Prescriptions above first and circulate for 8 hours!",
                    fc,
                    ph
                )
        }
    }
}
