package com.streamline.tv.data

/** Bundled photographic background scenes. */
enum class BackgroundStyle(val label: String) {
    AURORA_PEAKS("Aurora Peaks"),
    ALPINE_LAKE("Alpine Lake"),
    COASTAL_DUSK("Coastal Dusk"),
    FOREST_NIGHT("Forest Night"),
    VIDEO_WALLPAPER("Video wallpaper"),
}

enum class TimeOfDayMode(val label: String) {
    AUTO("Follow the clock"),
    DAY("Always day"),
    DUSK("Always dusk"),
    NIGHT("Always night"),
}

enum class TemperatureUnit(val label: String) {
    FAHRENHEIT("Fahrenheit"),
    CELSIUS("Celsius"),
}

enum class LayoutMode(val label: String) {
    SINGLE_ROW("Single row"),
    DOUBLE_ROW("Two rows"),
    GRID("Grid"),
}

/** Fire TV and mobile package names used by the MLB app. */
val MLB_PACKAGE_CANDIDATES: List<String> = listOf(
    // Fire TV's MLB app uses the older at-bat package name.
    "com.bamnetworks.mobile.android.gameday.atbat",
    "com.mlb.tv",
    "com.bamnetworks.mlbtv",
    "com.mlb.android",
)

/**
 * Installed-package candidates for the four tiles shown first on a fresh setup.
 * TV and mobile variants share a group so a device with both installed still
 * gets one tile per service. Earlier candidates win.
 */
val DEFAULT_APP_GROUPS: List<List<String>> = listOf(
    listOf("com.plexapp.android"),
    listOf(
        "com.google.android.youtube.tv",
        "com.google.android.youtube",
        "com.amazon.firetv.youtube",
    ),
    listOf(
        "com.google.android.apps.youtube.unplugged",
        "com.amazon.firetv.youtube.tv",
    ),
    MLB_PACKAGE_CANDIDATES,
)

/** All package variants seeded as favourites so later installs sort correctly. */
val DEFAULT_FAVORITES: Set<String> = DEFAULT_APP_GROUPS.flatten().toSet()

/** Removed from the fourth tile when MLB TV replaced Spotify. */
val LEGACY_SPOTIFY_PACKAGES: Set<String> = setOf(
    "com.spotify.tv.android",
    "com.spotify.music",
    "com.amazon.spotify.mediabrowserservice",
)

/**
 * Everything the launcher persists. Stored in a single Preferences DataStore
 * so writes are atomic and reads are a single flow.
 */
data class AppSettings(
    val backgroundStyle: BackgroundStyle = BackgroundStyle.AURORA_PEAKS,
    val timeOfDayMode: TimeOfDayMode = TimeOfDayMode.AUTO,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.FAHRENHEIT,
    val use24HourClock: Boolean = false,
    val showSeconds: Boolean = false,
    val showWeather: Boolean = true,
    val showAppLabels: Boolean = true,
    val reducedMotion: Boolean = false,
    val layoutMode: LayoutMode = LayoutMode.SINGLE_ROW,
    /** Off keeps Home limited to favourites; Manage apps still lists everything. */
    val showAllApps: Boolean = false,
    val favorites: Set<String> = DEFAULT_FAVORITES,
    val hiddenApps: Set<String> = emptySet(),
    /** When true the location is refreshed from IP geolocation on launch. */
    val autoDetectLocation: Boolean = true,
    val locationLabel: String = "Uniontown, Ohio",
    val latitude: Double = 40.968,
    val longitude: Double = -81.4167,
) {
    fun isFavorite(packageName: String) = packageName in favorites
    fun isHidden(packageName: String) = packageName in hiddenApps
}
