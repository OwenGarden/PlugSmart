package com.owengarden.plugsmart.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The app's local SQLite database, managed by Room. It has two tables:
 * - "prices": the most recent day of prices downloaded (for offline use)
 * - "usage_sessions": the user's power button history
 *
 * Version 2 added the usage_sessions table.
 */
@Database(
    entities = [PriceEntity::class, UsageSessionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PlugSmartDatabase : RoomDatabase() {
    abstract fun priceDao(): PriceDao
    abstract fun usageSessionDao(): UsageSessionDao
}
