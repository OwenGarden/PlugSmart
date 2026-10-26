package com.owengarden.plugsmart.ui.today

import com.owengarden.plugsmart.data.CheapestWindow
import com.owengarden.plugsmart.data.PriceLevel
import com.owengarden.plugsmart.data.PriceSlot
import com.owengarden.plugsmart.data.priceLevel
import kotlin.math.roundToInt

/**
 * Everything the Today screen needs to draw itself.
 * The ViewModel owns this; the screen only reads it.
 */
data class TodayUiState(
    val slots: List<PriceSlot> = emptyList(),   // all of today's half-hour slots
    val currentIndex: Int = 0,                  // which slot "now" is in (0 = 00:00)
    val averagePence: Double = 0.0,             // today's average price
    val bestWindow: CheapestWindow? = null,     // cheapest 3 hours from now onwards
    val cheapOnly: Boolean = false              // "only show cheap times" switch
) {
    /** The slot we are in right now, or null if there is no data. */
    val currentSlot: PriceSlot?
        get() = slots.getOrNull(currentIndex)

    /** Slots from now until the end of the day (past slots are not useful for planning). */
    val upcomingSlots: List<PriceSlot>
        get() = slots.drop(currentIndex)

    /** What the list shows, depending on the switch. */
    val visibleSlots: List<PriceSlot>
        get() = if (cheapOnly) upcomingSlots.filter { levelOf(it) == PriceLevel.CHEAP } else upcomingSlots

    /** How much cheaper the best window is than the day's average, e.g. 49. Null if it isn't cheaper. */
    val bestWindowSavingPercent: Int?
        get() {
            val window = bestWindow ?: return null
            if (averagePence <= 0.0) return null
            val saving = ((averagePence - window.averagePence) / averagePence * 100).roundToInt()
            return saving.takeIf { it > 0 }
        }

    fun levelOf(slot: PriceSlot): PriceLevel = priceLevel(slot.pricePence, averagePence)
}
