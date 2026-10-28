package com.owengarden.plugsmart.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * The one Octopus Energy API call the app makes.
 *
 * It returns the half-hourly Agile prices for North Scotland (region "P").
 * The API is public, so no API key or sign-in is needed.
 * Docs: https://developer.octopus.energy/docs/api/
 */
interface OctopusApi {

    /**
     * Gets every half-hour price between [periodFrom] and [periodTo].
     * Both are UTC times in ISO format, e.g. "2026-10-28T00:00:00Z".
     * 100 results is more than a full day (48 slots), so one page is enough.
     */
    @GET("v1/products/$PRODUCT/electricity-tariffs/E-1R-$PRODUCT-$REGION/standard-unit-rates/")
    suspend fun getPrices(
        @Query("period_from") periodFrom: String,
        @Query("period_to") periodTo: String,
        @Query("page_size") pageSize: Int = 100
    ): RatesResponse

    companion object {
        const val BASE_URL = "https://api.octopus.energy/"
        const val PRODUCT = "AGILE-24-10-01" // the current Agile tariff
        const val REGION = "P"               // North Scotland
    }
}
