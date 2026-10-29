package com.owengarden.plugsmart.data

import com.owengarden.plugsmart.data.local.UsageSessionDao
import com.owengarden.plugsmart.data.local.UsageSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Starts, stops, lists and deletes the user's power button sessions.
 * Everything is saved in Room, so a session keeps running even if the app is closed.
 */
class UsageRepository(private val dao: UsageSessionDao) {

    /** The session that is running right now, or null. */
    suspend fun getActiveSession(): UsageSession? = dao.getActive()?.toModel()

    /** The built-in choices, followed by anything the user has typed in before. */
    suspend fun applianceOptions(): List<String> =
        (DEFAULT_APPLIANCES + dao.getLabels()).distinct()

    /** Turns the power on. The name is tidied and limited in length before saving. */
    suspend fun start(label: String): UsageSession? {
        val cleanLabel = label.trim().take(MAX_LABEL_LENGTH)
        if (cleanLabel.isEmpty()) return null

        val entity = UsageSessionEntity(label = cleanLabel, startMillis = System.currentTimeMillis())
        val id = dao.insert(entity)
        return entity.copy(id = id).toModel()
    }

    /**
     * Turns the power off and works out the average price paid using today's [slots].
     * Sessions are capped at 24 hours in case the user forgot to turn the power off.
     */
    suspend fun finish(session: UsageSession, slots: List<PriceSlot>, dayAveragePence: Double): UsageSession {
        val endMillis = minOf(System.currentTimeMillis(), session.startMillis + MAX_SESSION_MILLIS)
        val finished = UsageSessionEntity(
            id = session.id,
            label = session.label,
            startMillis = session.startMillis,
            endMillis = endMillis,
            averagePence = averagePriceBetween(slots, session.startMillis, endMillis),
            dayAveragePence = dayAveragePence
        )
        dao.update(finished)
        return finished.toModel()
    }

    /** Finished sessions, newest first. Updates automatically when the table changes. */
    fun finishedSessions(): Flow<List<UsageSession>> =
        dao.getFinished().map { list -> list.map { it.toModel() } }

    suspend fun delete(id: Long) = dao.delete(id)

    private fun UsageSessionEntity.toModel() = UsageSession(
        id = id,
        label = label,
        startMillis = startMillis,
        endMillis = endMillis,
        averagePence = averagePence,
        dayAveragePence = dayAveragePence
    )

    companion object {
        val DEFAULT_APPLIANCES = listOf("Car charging", "Washing machine", "Dishwasher")
        const val MAX_LABEL_LENGTH = 40
        const val MAX_SESSION_MILLIS = 24 * 60 * 60 * 1000L // 24 hours
    }
}
