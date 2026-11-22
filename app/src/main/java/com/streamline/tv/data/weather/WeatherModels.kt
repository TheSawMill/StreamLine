package com.streamline.tv.data.weather

data class GeoLocation(
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val country: String = "",
)

enum class WeatherCondition(val label: String) {
    CLEAR("Clear"),
    PARTLY_CLOUDY("Partly cloudy"),
    CLOUDY("Cloudy"),
    FOG("Fog"),
    DRIZZLE("Drizzle"),
    RAIN("Rain"),
    SNOW("Snow"),
    THUNDERSTORM("Thunderstorm"),
    UNKNOWN("Unknown");

    companion object {
        /** Maps WMO weather interpretation codes to a small display enum. */
        fun fromWmo(code: Int): WeatherCondition = when (code) {
            0 -> CLEAR
            1, 2 -> PARTLY_CLOUDY
            3 -> CLOUDY
            45, 48 -> FOG
            51, 53, 55, 56, 57 -> DRIZZLE
            61, 63, 65, 66, 67, 80, 81, 82 -> RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 99 -> THUNDERSTORM
            else -> UNKNOWN
        }
    }
}

data class WeatherSnapshot(
    val temperature: Int,
    val feelsLike: Int,
    val high: Int,
    val low: Int,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val humidity: Int,
    val windSpeed: Int,
    val locationLabel: String,
    val updatedAt: Long,
)
