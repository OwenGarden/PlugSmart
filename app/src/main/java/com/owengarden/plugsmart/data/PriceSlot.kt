package com.owengarden.plugsmart.data

/**
 * One half-hour period of the day and its electricity price.
 *
 * For Phase 1 this is a plain data class filled with fake data.
 * Later it will be filled from the Octopus API and stored in Room.
 */
data class PriceSlot(
    val startTime: String,   // e.g. "02:00"
    val endTime: String,     // e.g. "02:30"
    val pricePence: Double   // price in pence per kWh, including VAT
)
