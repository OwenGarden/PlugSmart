package com.owengarden.plugsmart.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

/**
 * The database queries the app needs. Room writes the code for the abstract functions.
 * It's an abstract class (not an interface) so replaceAll can have its own body.
 */
@Dao
abstract class PriceDao {

    /** All saved prices, earliest first. */
    @Query("SELECT * FROM prices ORDER BY startMillis")
    abstract suspend fun getAll(): List<PriceEntity>

    @Insert
    abstract suspend fun insertAll(prices: List<PriceEntity>)

    @Query("DELETE FROM prices")
    abstract suspend fun deleteAll()

    /**
     * Swaps the old saved day for the new one.
     * @Transaction means both steps happen together, so the table is never left half-empty.
     */
    @Transaction
    open suspend fun replaceAll(prices: List<PriceEntity>) {
        deleteAll()
        insertAll(prices)
    }
}
