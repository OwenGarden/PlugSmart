package com.owengarden.plugsmart.data

import android.content.Context
import androidx.room.Room
import com.owengarden.plugsmart.data.local.PlugSmartDatabase
import com.owengarden.plugsmart.data.remote.OctopusApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Creates the app's shared objects once (the API client, the database and the repositories)
 * so every screen uses the same ones. This is simple manual dependency injection,
 * as used in the Android Basics with Compose course.
 */
class AppContainer(context: Context) {

    private val api: OctopusApi = Retrofit.Builder()
        .baseUrl(OctopusApi.BASE_URL) // HTTPS, so prices are encrypted in transit
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OctopusApi::class.java)

    private val database: PlugSmartDatabase = Room.databaseBuilder(
        context,
        PlugSmartDatabase::class.java,
        "plugsmart.db"
    )
        // When the database version goes up (e.g. a new table), start with a fresh database.
        // Fine for this app: the prices are downloaded again straight away.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    val priceRepository = PriceRepository(api, database.priceDao())
    val usageRepository = UsageRepository(database.usageSessionDao())
}
