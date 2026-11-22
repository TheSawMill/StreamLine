package com.streamline.tv.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "streamline_settings")

class SettingsRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    val settings: Flow<AppSettings> = store.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { it.toSettings() }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        store.edit { preferences ->
            preferences.write(transform(preferences.toSettings()))
        }
    }

    /**
     * Seed the default tiles once per defaults version so existing installs get
     * new package variants without losing choices made after the migration.
     */
    suspend fun ensureDefaultFavorites() = store.edit { preferences ->
        if (preferences[Keys.DEFAULTS_SEEDED] != true) {
            preferences[Keys.FAVORITES] =
                ((preferences[Keys.FAVORITES] ?: emptySet()) - LEGACY_SPOTIFY_PACKAGES) + DEFAULT_FAVORITES
            preferences[Keys.DEFAULTS_SEEDED] = true
        }
    }

    suspend fun setFavorite(packageName: String, favorite: Boolean) = update { current ->
        if (favorite) current.copy(favorites = current.favorites + packageName)
        else current.copy(favorites = current.favorites - packageName)
    }

    suspend fun setHidden(packageName: String, hidden: Boolean) = update { current ->
        if (hidden) current.copy(hiddenApps = current.hiddenApps + packageName)
        else current.copy(hiddenApps = current.hiddenApps - packageName)
    }

    private fun Preferences.toSettings(): AppSettings = AppSettings(
        backgroundStyle = readEnum(Keys.BACKGROUND, BackgroundStyle.entries, BackgroundStyle.AURORA_PEAKS),
        timeOfDayMode = readEnum(Keys.TIME_OF_DAY, TimeOfDayMode.entries, TimeOfDayMode.AUTO),
        temperatureUnit = readEnum(Keys.TEMPERATURE_UNIT, TemperatureUnit.entries, TemperatureUnit.FAHRENHEIT),
        use24HourClock = this[Keys.USE_24_HOUR] ?: false,
        showSeconds = this[Keys.SHOW_SECONDS] ?: false,
        showWeather = this[Keys.SHOW_WEATHER] ?: true,
        showAppLabels = this[Keys.SHOW_APP_LABELS] ?: true,
        reducedMotion = this[Keys.REDUCED_MOTION] ?: false,
        layoutMode = readEnum(Keys.LAYOUT, LayoutMode.entries, LayoutMode.SINGLE_ROW),
        showAllApps = this[Keys.SHOW_ALL_APPS] ?: false,
        favorites = this[Keys.FAVORITES] ?: DEFAULT_FAVORITES,
        hiddenApps = this[Keys.HIDDEN_APPS] ?: emptySet(),
        autoDetectLocation = this[Keys.AUTO_LOCATION] ?: true,
        locationLabel = this[Keys.LOCATION_LABEL] ?: "Uniontown, Ohio",
        latitude = this[Keys.LATITUDE] ?: 40.968,
        longitude = this[Keys.LONGITUDE] ?: -81.4167,
    )

    private fun androidx.datastore.preferences.core.MutablePreferences.write(settings: AppSettings) {
        this[Keys.BACKGROUND] = settings.backgroundStyle.name
        this[Keys.TIME_OF_DAY] = settings.timeOfDayMode.name
        this[Keys.TEMPERATURE_UNIT] = settings.temperatureUnit.name
        this[Keys.USE_24_HOUR] = settings.use24HourClock
        this[Keys.SHOW_SECONDS] = settings.showSeconds
        this[Keys.SHOW_WEATHER] = settings.showWeather
        this[Keys.SHOW_APP_LABELS] = settings.showAppLabels
        this[Keys.REDUCED_MOTION] = settings.reducedMotion
        this[Keys.LAYOUT] = settings.layoutMode.name
        this[Keys.SHOW_ALL_APPS] = settings.showAllApps
        this[Keys.FAVORITES] = settings.favorites
        this[Keys.HIDDEN_APPS] = settings.hiddenApps
        this[Keys.AUTO_LOCATION] = settings.autoDetectLocation
        this[Keys.LOCATION_LABEL] = settings.locationLabel
        this[Keys.LATITUDE] = settings.latitude
        this[Keys.LONGITUDE] = settings.longitude
    }

    private inline fun <reified T : Enum<T>> Preferences.readEnum(
        key: Preferences.Key<String>,
        entries: List<T>,
        fallback: T,
    ): T = this[key]?.let { stored -> entries.firstOrNull { it.name == stored } } ?: fallback

    private object Keys {
        val BACKGROUND = stringPreferencesKey("background_style")
        val TIME_OF_DAY = stringPreferencesKey("time_of_day_mode")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val USE_24_HOUR = booleanPreferencesKey("use_24_hour_clock")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val SHOW_WEATHER = booleanPreferencesKey("show_weather")
        val SHOW_APP_LABELS = booleanPreferencesKey("show_app_labels")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val LAYOUT = stringPreferencesKey("layout_mode")
        val SHOW_ALL_APPS = booleanPreferencesKey("show_all_apps")
        val FAVORITES = stringSetPreferencesKey("favorites")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val DEFAULTS_SEEDED = booleanPreferencesKey("defaults_seeded_v3")
        val AUTO_LOCATION = booleanPreferencesKey("auto_detect_location")
        val LOCATION_LABEL = stringPreferencesKey("location_label")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
    }
}
