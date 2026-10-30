package com.owengarden.plugsmart.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.owengarden.plugsmart.PlugSmartApplication
import retrofit2.HttpException
import java.io.IOException
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Background job that downloads the latest prices into Room once a day, even if
 * the app isn't open. It runs after Octopus publishes tomorrow's prices (around 4pm),
 * so the app has tomorrow's real prices ready even if the phone is offline tomorrow.
 *
 * WorkManager decides exactly when to run it (Android may delay it slightly to save
 * battery) and keeps the schedule after the phone restarts.
 */
class PriceSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as PlugSmartApplication).container.priceRepository
        return try {
            repository.refreshPrices()
            Result.success()
        } catch (e: IOException) {
            retryOrGiveUp()   // no internet or no prices yet
        } catch (e: HttpException) {
            retryOrGiveUp()   // Octopus server error
        }
    }

    /** Try again a few times (WorkManager waits longer each time), then wait for tomorrow's run. */
    private fun retryOrGiveUp(): Result =
        if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()

    companion object {
        private const val WORK_NAME = "daily_price_sync"
        private const val MAX_ATTEMPTS = 3
        private val SYNC_TIME: LocalTime = LocalTime.of(17, 0) // after the ~4pm price release

        /**
         * Schedules the daily sync. Called every time the app starts, but KEEP means
         * an existing schedule is left alone, so jobs never stack up.
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PriceSyncWorker>(24, TimeUnit.HOURS)
                // Only run when there's an internet connection; otherwise wait for one.
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                // First run at the next 5pm, then every 24 hours.
                .setInitialDelay(delayUntilNextSync().toMillis(), TimeUnit.MILLISECONDS)
                // If it fails, wait 15 minutes, then 30, then 60 before trying again.
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /** How long from now until the next 5pm UK time. */
        private fun delayUntilNextSync(): Duration {
            val ukZone = ZoneId.of("Europe/London")
            val now = ZonedDateTime.now(ukZone)
            var next = LocalDate.now(ukZone).atTime(SYNC_TIME).atZone(ukZone)
            if (!next.isAfter(now)) next = next.plusDays(1)
            return Duration.between(now, next)
        }
    }
}
