package com.owengarden.plugsmart.data

/** The cheapest block of time found for running an appliance. */
data class CheapestWindow(
    val startTime: String,
    val endTime: String,
    val averagePence: Double
)

/** How a slot's price compares to the day's average. Used to colour the list. */
enum class PriceLevel { CHEAP, NORMAL, EXPENSIVE }

/**
 * Finds the cheapest run of [slotCount] back-to-back half-hour slots.
 *
 * Uses a sliding window: start with the total of the first [slotCount] slots,
 * then move along one slot at a time, adding the new slot and removing the old one.
 * Returns null if there are fewer slots than [slotCount].
 */
fun findCheapestWindow(slots: List<PriceSlot>, slotCount: Int): CheapestWindow? {
    if (slotCount <= 0 || slots.size < slotCount) return null

    var windowTotal = slots.take(slotCount).sumOf { it.pricePence }
    var bestTotal = windowTotal
    var bestStart = 0

    for (start in 1..slots.size - slotCount) {
        windowTotal += slots[start + slotCount - 1].pricePence - slots[start - 1].pricePence
        if (windowTotal < bestTotal) {
            bestTotal = windowTotal
            bestStart = start
        }
    }

    return CheapestWindow(
        startTime = slots[bestStart].startTime,
        endTime = slots[bestStart + slotCount - 1].endTime,
        averagePence = bestTotal / slotCount
    )
}

/** Cheap = 20% or more below average, expensive = 20% or more above average. */
fun priceLevel(pricePence: Double, averagePence: Double): PriceLevel = when {
    pricePence <= averagePence * 0.8 -> PriceLevel.CHEAP
    pricePence >= averagePence * 1.2 -> PriceLevel.EXPENSIVE
    else -> PriceLevel.NORMAL
}
