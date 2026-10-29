package com.owengarden.plugsmart.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row in the "usage_sessions" table: a time the user turned the power button on and off.
 * While the power is still on, [endMillis] is null. Only one row is ever "on" at a time.
 */
@Entity(tableName = "usage_sessions")
data class UsageSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,                   // what was used, e.g. "Car charging" or "Ran a bath"
    val startMillis: Long,
    val endMillis: Long? = null,         // null while the power is on
    val averagePence: Double? = null,    // average price paid, worked out when turned off
    val dayAveragePence: Double? = null  // that day's average, to colour it cheap/normal/expensive
)
