package com.owengarden.plugsmart.data

/**
 * One half-hour period of the day and its electricity price. This is what the UI uses.
 *
 * The times as text are for display. The millisecond times are used to find
 * "right now" and as a unique key for each row in the list (on the day the clocks
 * go back, "01:00" happens twice, so the text alone isn't unique).
 */
data class PriceSlot(
    val startTime: String,     // e.g. "02:00" (UK time)
    val endTime: String,       // e.g. "02:30"
    val pricePence: Double,    // price in pence per kWh, including VAT
    val startMillis: Long = 0L,
    val endMillis: Long = 0L
)
