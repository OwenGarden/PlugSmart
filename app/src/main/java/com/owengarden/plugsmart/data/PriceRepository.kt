package com.owengarden.plugsmart.data

import com.owengarden.plugsmart.data.local.PriceDao
import com.owengarden.plugsmart.data.local.PriceEntity
import com.owengarden.plugsmart.data.remote.OctopusApi
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** What the repository hands back to the ViewModel. */
data class PriceResult(
    val slots: List<PriceSlot>,
    val savedDate: LocalDate? = null // set when offline: the day the saved prices are from
) {
    val isFromCache: Boolean get() = savedDate != null
}

/**
 * The single place the app gets prices from (the Repository pattern).
 * The ViewModel doesn't know or care whether prices came from the internet or the database.
 *
 * 1. Try the Octopus API for today's prices.
 * 2. If that works, save them in Room (replacing the old day) and return them.
 * 3. If the internet fails, return the saved prices instead, moved onto today's
 *    times so they can be used as a guide.
 * 4. If there is nothing saved either, throw so the ViewModel can show an error.
 */
class PriceRepository(
    private val api: OctopusApi,
    private val dao: PriceDao
) {
    private val ukZone = ZoneId.of("Europe/London")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    suspend fun getTodaysPrices(): PriceResult {
        val today = LocalDate.now(ukZone)
        return try {
            // Today's UK midnight to tomorrow's UK midnight, converted to UTC for the API.
            val response = api.getPrices(
                periodFrom = today.atStartOfDay(ukZone).toInstant().toString(),
                periodTo = today.plusDays(1).atStartOfDay(ukZone).toInstant().toString()
            )
            // The API sends newest first, so sort into time order.
            val entities = response.results
                .map {
                    PriceEntity(
                        startMillis = Instant.parse(it.validFrom).toEpochMilli(),
                        endMillis = Instant.parse(it.validTo).toEpochMilli(),
                        pricePence = it.valueIncVat
                    )
                }
                .sortedBy { it.startMillis }

            if (entities.isEmpty()) throw IOException("The API returned no prices")

            dao.replaceAll(entities)
            PriceResult(slots = entities.map { it.toPriceSlot() })
        } catch (e: IOException) {
            savedPricesOrThrow(today, e)   // no internet, timeout, or empty response
        } catch (e: HttpException) {
            savedPricesOrThrow(today, e)   // the server replied with an error (e.g. 500)
        }
    }

    /** Falls back to the database, or re-throws the original error if it's empty. */
    private suspend fun savedPricesOrThrow(today: LocalDate, error: Exception): PriceResult {
        val saved = dao.getAll()
        if (saved.isEmpty()) throw error

        val savedDate = Instant.ofEpochMilli(saved.first().startMillis).atZone(ukZone).toLocalDate()
        // Move yesterday's (or older) prices onto today so "right now" and the best time still work.
        val daysOld = ChronoUnit.DAYS.between(savedDate, today)
        val slots = saved.map { it.shiftDays(daysOld).toPriceSlot() }

        return PriceResult(slots = slots, savedDate = savedDate)
    }

    /** Moves a saved slot forward by whole days, keeping the same UK clock time. */
    private fun PriceEntity.shiftDays(days: Long): PriceEntity {
        if (days == 0L) return this
        return copy(
            startMillis = Instant.ofEpochMilli(startMillis).atZone(ukZone).plusDays(days).toInstant().toEpochMilli(),
            endMillis = Instant.ofEpochMilli(endMillis).atZone(ukZone).plusDays(days).toInstant().toEpochMilli()
        )
    }

    /** Converts a database row into what the UI uses, showing times in UK time. */
    private fun PriceEntity.toPriceSlot() = PriceSlot(
        startTime = Instant.ofEpochMilli(startMillis).atZone(ukZone).format(timeFormat),
        endTime = Instant.ofEpochMilli(endMillis).atZone(ukZone).format(timeFormat),
        pricePence = pricePence,
        startMillis = startMillis,
        endMillis = endMillis
    )
}
