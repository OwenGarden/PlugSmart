package com.owengarden.plugsmart.ui.history

import com.owengarden.plugsmart.data.UsageSession

/** Everything the History screen needs to draw itself. */
data class HistoryUiState(
    val isLoading: Boolean = true,
    val sessions: List<UsageSession> = emptyList() // finished sessions, newest first
) {
    /** Sessions where the price paid is known. */
    private val pricedSessions: List<UsageSession>
        get() = sessions.filter { it.averagePence != null && it.durationMillis > 0 }

    val pricedCount: Int
        get() = pricedSessions.size

    /**
     * The user's own average price, weighted by how long each session lasted,
     * so a 4-hour car charge counts for more than a 10-minute kettle.
     * Null if there's nothing to average yet.
     */
    val personalAveragePence: Double?
        get() {
            val totalMillis = pricedSessions.sumOf { it.durationMillis }
            if (totalMillis == 0L) return null
            return pricedSessions.sumOf { it.averagePence!! * it.durationMillis } / totalMillis
        }
}
