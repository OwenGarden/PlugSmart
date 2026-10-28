package com.owengarden.plugsmart.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row in the "prices" table: a saved half-hour price.
 * Times are stored as milliseconds since 1970 (UTC), which avoids time zone mix-ups.
 */
@Entity(tableName = "prices")
data class PriceEntity(
    @PrimaryKey val startMillis: Long, // each slot starts at a unique time
    val endMillis: Long,
    val pricePence: Double             // pence per kWh, including VAT
)
