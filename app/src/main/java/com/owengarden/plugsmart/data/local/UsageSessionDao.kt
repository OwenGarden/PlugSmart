package com.owengarden.plugsmart.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Database queries for the power button and the history screen. */
@Dao
interface UsageSessionDao {

    /** The session that is still running (power on), or null if the power is off. */
    @Query("SELECT * FROM usage_sessions WHERE endMillis IS NULL LIMIT 1")
    suspend fun getActive(): UsageSessionEntity?

    /**
     * Finished sessions, newest first. Returns a Flow, so the history screen
     * updates by itself when a session is added or deleted.
     */
    @Query("SELECT * FROM usage_sessions WHERE endMillis IS NOT NULL ORDER BY startMillis DESC")
    fun getFinished(): Flow<List<UsageSessionEntity>>

    /** Every name the user has used before, so their own appliances appear as choices. */
    @Query("SELECT DISTINCT label FROM usage_sessions ORDER BY label")
    suspend fun getLabels(): List<String>

    /** Saves a new session and returns its id. */
    @Insert
    suspend fun insert(session: UsageSessionEntity): Long

    @Update
    suspend fun update(session: UsageSessionEntity)

    @Query("DELETE FROM usage_sessions WHERE id = :id")
    suspend fun delete(id: Long)
}
