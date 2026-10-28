package com.owengarden.plugsmart.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.owengarden.plugsmart.PlugSmartApplication
import com.owengarden.plugsmart.data.PriceRepository
import com.owengarden.plugsmart.data.findCheapestWindow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Holds the Today screen's state.
 *
 * A ViewModel survives configuration changes such as screen rotation,
 * so the prices aren't downloaded again every time the phone is turned.
 */
class TodayViewModel(
    private val repository: PriceRepository
) : ViewModel() {

    // Private and mutable: only the ViewModel can change the state.
    private val _uiState = MutableStateFlow(TodayUiState())

    // Public and read-only: the screen collects this and redraws when it changes.
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private val ukZone = ZoneId.of("Europe/London")

    init {
        loadPrices()
    }

    /** Called on start and when the user taps Retry. */
    fun loadPrices() {
        _uiState.update { it.copy(isLoading = true, hasError = false) }

        // viewModelScope runs this in the background and cancels it if the ViewModel is cleared.
        viewModelScope.launch {
            try {
                val result = repository.getTodaysPrices()
                val slots = result.slots

                // Find the slot that contains the current time. Fall back to the first slot.
                val now = System.currentTimeMillis()
                val nowIndex = slots.indexOfFirst { now >= it.startMillis && now < it.endMillis }
                    .coerceAtLeast(0)

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        slots = slots,
                        currentIndex = nowIndex,
                        averagePence = slots.map { it.pricePence }.average(),
                        // Only look at slots from now onwards - a cheap time that has passed is no use.
                        // 6 slots x 30 minutes = 3 hours.
                        bestWindow = findCheapestWindow(slots.drop(nowIndex), slotCount = 6),
                        lastUpdated = if (result.isFromCache) null
                            else LocalTime.now(ukZone).format(DateTimeFormatter.ofPattern("HH:mm")),
                        offlineDate = result.savedDate?.format(DateTimeFormatter.ofPattern("EEE d MMM"))
                    )
                }
            } catch (e: IOException) {
                // No internet and nothing saved yet
                _uiState.update { it.copy(isLoading = false, hasError = true) }
            } catch (e: HttpException) {
                // Server error and nothing saved yet
                _uiState.update { it.copy(isLoading = false, hasError = true) }
            }
        }
    }

    /** Called when the user flips the "only show cheap times" switch. */
    fun onCheapOnlyChanged(enabled: Boolean) {
        _uiState.update { it.copy(cheapOnly = enabled) }
    }

    companion object {
        /**
         * Tells Compose how to build this ViewModel, because it needs the repository
         * passed in. The repository comes from the AppContainer in PlugSmartApplication.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PlugSmartApplication
                TodayViewModel(app.container.priceRepository)
            }
        }
    }
}
