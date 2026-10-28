package com.owengarden.plugsmart.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Matches the JSON the Octopus API sends back. Gson fills these in automatically.
 * Only the fields the app uses are listed - Gson ignores the rest.
 *
 * Example:
 * { "results": [ { "value_inc_vat": 12.1,
 *                  "valid_from": "2026-10-28T12:30:00Z",
 *                  "valid_to":   "2026-10-28T13:00:00Z" } ] }
 */
data class RatesResponse(
    @SerializedName("results") val results: List<RateDto>
)

/** One half-hour price as sent by the API. Times are in UTC. */
data class RateDto(
    @SerializedName("value_inc_vat") val valueIncVat: Double,
    @SerializedName("valid_from") val validFrom: String,
    @SerializedName("valid_to") val validTo: String
)
