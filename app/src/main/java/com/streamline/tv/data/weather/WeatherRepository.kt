package com.streamline.tv.data.weather

import com.streamline.tv.data.TemperatureUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Open-Meteo is used because it needs no API key, which keeps the launcher
 * usable out of the box and avoids shipping secrets in the APK.
 */
class WeatherRepository {

    suspend fun fetch(
        location: GeoLocation,
        unit: TemperatureUnit,
    ): Result<WeatherSnapshot> = withContext(Dispatchers.IO) {
        runCatching {
            val temperatureUnit = if (unit == TemperatureUnit.FAHRENHEIT) "fahrenheit" else "celsius"
            val windUnit = if (unit == TemperatureUnit.FAHRENHEIT) "mph" else "kmh"
            val url = buildString {
                append("https://api.open-meteo.com/v1/forecast")
                append("?latitude=").append(location.latitude)
                append("&longitude=").append(location.longitude)
                append("&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m")
                append("&daily=temperature_2m_max,temperature_2m_min")
                append("&forecast_days=1&timezone=auto")
                append("&temperature_unit=").append(temperatureUnit)
                append("&wind_speed_unit=").append(windUnit)
            }

            val json = JSONObject(httpGet(url))
            val current = json.getJSONObject("current")
            val daily = json.getJSONObject("daily")

            WeatherSnapshot(
                temperature = current.getDouble("temperature_2m").roundToInt(),
                feelsLike = current.getDouble("apparent_temperature").roundToInt(),
                high = daily.getJSONArray("temperature_2m_max").getDouble(0).roundToInt(),
                low = daily.getJSONArray("temperature_2m_min").getDouble(0).roundToInt(),
                condition = WeatherCondition.fromWmo(current.getInt("weather_code")),
                isDay = current.optInt("is_day", 1) == 1,
                humidity = current.optInt("relative_humidity_2m"),
                windSpeed = current.optDouble("wind_speed_10m", 0.0).roundToInt(),
                locationLabel = location.label,
                updatedAt = System.currentTimeMillis(),
            )
        }
    }

    /** City search for the manual location picker (Open-Meteo geocoding, no key). */
    suspend fun searchLocations(query: String): List<GeoLocation> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        runCatching {
            val encoded = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=8&language=en&format=json"
            val json = JSONObject(httpGet(url))
            val results = json.optJSONArray("results") ?: return@runCatching emptyList()
            buildList {
                for (index in 0 until results.length()) {
                    val item = results.getJSONObject(index)
                    val name = item.optString("name")
                    val region = item.optString("admin1")
                    val country = item.optString("country")
                    val label = listOf(name, region, country)
                        .filter { it.isNotBlank() }
                        .distinct()
                        .joinToString(", ")
                    add(
                        GeoLocation(
                            label = label.ifBlank { name },
                            latitude = item.getDouble("latitude"),
                            longitude = item.getDouble("longitude"),
                            country = country,
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
