package com.owengarden.plugsmart.ui.today

import androidx.lifecycle.ViewModel
import com.owengarden.plugsmart.data.FakePriceData
import com.owengarden.plugsmart.data.findCheapestWindow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar

/**
 * Holds the Today screen's state.
 *
 * A ViewModel survives configuration changes such as screen rotation,
 * so the prices and the switch position are kept when the phone is turned.
 */
class TodayViewModel : ViewModel() {

    // Private and mutable: only the ViewModel can change the state.
    private val _uiState = MutableStateFlow(TodayUiState())

    // Public and read-only: the screen collects this and redraws when it changes.
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadPrices()
    }

    /** Loads today's prices. Uses fake data for now; the Octopus API comes in Phase 2. */
    private fun loadPrices() {
        val slots = FakePriceData.todaysPrices()
        val nowIndex = currentSlotIndex()

        _uiState.update { current ->
            current.copy(
                slots = slots,
                currentIndex = nowIndex,
                averagePence = slots.map { it.pricePence }.average(),
                // Only look at slots from now onwards - a cheap time that has passed is no use.
                // 6 slots x 30 minutes = 3 hours.
                bestWindow = findCheapestWindow(slots.drop(nowIndex), slotCount = 6)
            )
        }
    }

    /** Called when the user flips the "only show cheap times" switch. */
    fun onCheapOnlyChanged(enabled: Boolean) {
        _uiState.update { it.copy(cheapOnly = enabled) }
    }

    /** Works out which half-hour slot the current time falls in (00:00 = 0, 13:30 = 27). */
    private fun currentSlotIndex(): Int {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        return hour * 2 + if (minute >= 30) 1 else 0
    }
}
