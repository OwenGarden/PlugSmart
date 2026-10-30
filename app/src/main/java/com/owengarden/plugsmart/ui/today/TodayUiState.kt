package com.owengarden.plugsmart.ui.today

import com.owengarden.plugsmart.data.CheapestWindow
import com.owengarden.plugsmart.data.PriceLevel
import com.owengarden.plugsmart.data.PriceSlot
import com.owengarden.plugsmart.data.UsageSession
import com.owengarden.plugsmart.data.priceLevel
import kotlin.math.roundToInt

/**
 * Everything the Today screen needs to draw itself.
 * The ViewModel owns this; the screen only reads it.
 */
data class TodayUiState(
    val isLoading: Boolean = true,              // true while prices are being fetched
    val hasError: Boolean = false,              // true if nothing could be loaded at all
    val lastUpdated: String? = null,            // e.g. "12:31", when the prices were downloaded
    val isOffline: Boolean = false,             // true when showing saved prices (no internet)
    val offlineDate: String? = null,            // e.g. "Tue 27 Oct" when an older day is used as a guide
    val slots: List<PriceSlot> = emptyList(),   // all of today's half-hour slots
    val currentIndex: Int = 0,                  // which slot "now" is in (0 = 00:00)
    val averagePence: Double = 0.0,             // today's average price
    val bestWindow: CheapestWindow? = null,     // cheapest 3 hours from now onwards

    // Power button
    val activeSession: UsageSession? = null,    // the session running now, or null if power is off
    val showStartDialog: Boolean = false,       // the "What are you using?" dialog
    val applianceOptions: List<String> = emptyList(),
    val finishedSession: UsageSession? = null   // shows the result dialog after turning off
) {
    /** The slot we are in right now, or null if there is no data. */
    val currentSlot: PriceSlot?
        get() = slots.getOrNull(currentIndex)

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
