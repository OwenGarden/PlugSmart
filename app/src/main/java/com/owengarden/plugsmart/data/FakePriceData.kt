package com.owengarden.plugsmart.data

import java.util.Locale

/**
 * Hard-coded prices, now only used for the Compose previews in Android Studio.
 * Shaped like a typical Agile day: cheap overnight, most expensive 16:00–19:00.
 */
object FakePriceData {

    private const val HALF_HOUR_MILLIS = 30 * 60 * 1000L

    // 48 half-hourly prices in p/kWh, starting at 00:00.
    private val prices = listOf(
        14.2, 13.8, 12.9, 11.5, 9.8, 8.7, 8.2, 8.0, 8.4, 9.1, 10.3, 12.6,      // 00:00–05:30
        15.4, 18.2, 20.1, 21.3, 19.8, 18.5, 17.2, 16.0, 15.1, 14.6, 13.9, 13.2, // 06:00–11:30
        12.8, 12.1, 11.7, 12.4, 13.6, 15.9, 18.7, 22.4, 31.5, 34.8, 35.2, 33.9, // 12:00–17:30
        30.1, 26.4, 22.8, 20.5, 19.2, 18.4, 17.6, 16.9, 16.1, 15.5, 14.9, 14.5  // 18:00–23:30
    )

    /** Builds today's list of 48 half-hour slots from the prices above. */
    fun todaysPrices(): List<PriceSlot> = prices.mapIndexed { index, price ->
        PriceSlot(
            startTime = slotTime(index),
            endTime = slotTime(index + 1),
            pricePence = price,
            startMillis = index * HALF_HOUR_MILLIS,
            endMillis = (index + 1) * HALF_HOUR_MILLIS
        )
    }

    /** Turns a slot number (0 = 00:00, 1 = 00:30, ...) into "HH:mm". Slot 48 wraps to "00:00". */
    private fun slotTime(index: Int): String {
        val minutes = (index * 30) % (24 * 60)
        return String.format(Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60)
    }
}
