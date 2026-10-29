package com.owengarden.plugsmart.data

/**
 * One time the user used electricity, as shown in the app.
 * [endMillis] is null while the power button is still on.
 */
data class UsageSession(
    val id: Long,
    val label: String,
    val startMillis: Long,
    val endMillis: Long?,
    val averagePence: Double?,    // null if no prices covered that time
    val dayAveragePence: Double?
) {
    val isRunning: Boolean get() = endMillis == null

    /** How long it lasted, in milliseconds (0 while still running). */
    val durationMillis: Long get() = (endMillis ?: startMillis) - startMillis
}
