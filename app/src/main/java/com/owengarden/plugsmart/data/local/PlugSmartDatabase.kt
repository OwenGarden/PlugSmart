package com.owengarden.plugsmart.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The app's local SQLite database, managed by Room.
 * It has one table ("prices") holding the most recent day downloaded.
 */
@Database(entities = [PriceEntity::class], version = 1, exportSchema = false)
abstract class PlugSmartDatabase : RoomDatabase() {
    abstract fun priceDao(): PriceDao
}
