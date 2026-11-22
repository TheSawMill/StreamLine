package com.streamline.tv.data.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Approximate location from the public IP address. This avoids asking for the
 * ACCESS_COARSE_LOCATION permission on a TV, where permission prompts are
 * awkward and location services are often unavailable on Fire TV sticks.
 */
class LocationResolver {

    suspend fun detect(): GeoLocation? = withContext(Dispatchers.IO) {
        runCatching {
            val json = JSONObject(httpGet("https://ipinfo.io/json"))
            val coordinates = json.optString("loc").split(",")
            if (coordinates.size != 2) return@runCatching null
            val latitude = coordinates[0].toDoubleOrNull() ?: return@runCatching null
            val longitude = coordinates[1].toDoubleOrNull() ?: return@runCatching null
            val city = json.optString("city")
            val region = json.optString("region")
            val label = listOf(city, region)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(", ")
            GeoLocation(
                label = label.ifBlank { "My area" },
                latitude = latitude,
                longitude = longitude,
                country = json.optString("country"),
            )
        }.getOrNull()
    }
}
