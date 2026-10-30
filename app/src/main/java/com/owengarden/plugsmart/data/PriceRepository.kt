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
    val isOffline: Boolean = false,  // true if the internet failed and saved prices were used
    val guessFromDate: LocalDate? = null // set if no prices were saved for today, so an older day is used as a guide
)

/**
 * The single place the app gets prices from (the Repository pattern).
 * The ViewModel doesn't know or care whether prices came from the internet or the database.
 *
 * Room holds up to two days of prices: today, plus tomorrow once Octopus has
 * published it (around 4pm). The background sync in PriceSyncWorker keeps this up to date.
 *
 * When the screen asks for today's prices:
 * 1. Try the Octopus API. If that works, save the prices and return today's.
 * 2. If the internet fails, use today's saved prices (downloaded earlier, e.g. by the sync).
 * 3. If none were saved for today, use the most recent saved day, moved onto today's
 *    times, as a guide.
 * 4. If nothing is saved at all, throw so the ViewModel can show an error.
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
            val saved = refreshPrices()
            PriceResult(slots = saved.onDay(today).map { it.toPriceSlot() })
        } catch (e: IOException) {
            savedPricesOrThrow(today, e)   // no internet, timeout, or empty response
        } catch (e: HttpException) {
            savedPricesOrThrow(today, e)   // the server replied with an error (e.g. 500)
        }
    }

    /**
     * Downloads today's and tomorrow's prices and saves them in Room, replacing what was there.
     * Used by the screen and by the background sync. Throws if the download fails.
     * Before 4pm tomorrow's prices aren't published yet, so only today's come back.
     */
    suspend fun refreshPrices(): List<PriceEntity> {
        val today = LocalDate.now(ukZone)
        // Today's UK midnight to the day after tomorrow's UK midnight, converted to UTC for the API.
        val response = api.getPrices(
            periodFrom = today.atStartOfDay(ukZone).toInstant().toString(),
            periodTo = today.plusDays(2).atStartOfDay(ukZone).toInstant().toString()
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
        return entities
    }

    /** Falls back to the database, or re-throws the original error if it's empty. */
    private suspend fun savedPricesOrThrow(today: LocalDate, error: Exception): PriceResult {
        val saved = dao.getAll()
        if (saved.isEmpty()) throw error

        // Best case: today's real prices were saved earlier.
        val todays = saved.onDay(today)
        if (todays.isNotEmpty()) {
            return PriceResult(slots = todays.map { it.toPriceSlot() }, isOffline = true)
        }

        // Otherwise use the most recent saved day before today as a guide.
        val days = saved.map { it.ukDate() }.distinct()
        // (Compared by epoch day number, because LocalDate isn't directly sortable in Kotlin.)
        val guessDay = days.filter { it < today }.maxByOrNull { it.toEpochDay() }
            ?: days.minBy { it.toEpochDay() }
        val daysOld = ChronoUnit.DAYS.between(guessDay, today)
        val slots = saved.onDay(guessDay).map { it.shiftDays(daysOld).toPriceSlot() }

        return PriceResult(slots = slots, isOffline = true, guessFromDate = guessDay)
    }

    /** The UK date a saved slot starts on. */
    private fun PriceEntity.ukDate(): LocalDate =
        Instant.ofEpochMilli(startMillis).atZone(ukZone).toLocalDate()

    /** Only the slots that start on [day]. */
    private fun List<PriceEntity>.onDay(day: LocalDate): List<PriceEntity> =
        filter { it.ukDate() == day }

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
