package com.owengarden.plugsmart.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.owengarden.plugsmart.PlugSmartApplication
import com.owengarden.plugsmart.data.UsageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Holds the History screen's state.
 * The list comes straight from Room as a Flow, so deleting an entry
 * updates the list and the average automatically.
 */
class HistoryViewModel(
    private val usageRepository: UsageRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = usageRepository.finishedSessions()
        .map { sessions -> HistoryUiState(isLoading = false, sessions = sessions) }
        .stateIn(
            scope = viewModelScope,
            // Keep listening for 5 seconds after the screen stops (e.g. during rotation).
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState()
        )

    /** Removes an entry, e.g. if the power button was pressed by accident. */
    fun deleteSession(id: Long) {
        viewModelScope.launch {
            usageRepository.delete(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PlugSmartApplication
                HistoryViewModel(app.container.usageRepository)
            }
        }
    }
}
