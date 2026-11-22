package com.streamline.tv.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.streamline.tv.StreamlineApplication
import com.streamline.tv.data.AppEntry
import com.streamline.tv.data.AppSettings
import com.streamline.tv.data.DEFAULT_APP_GROUPS
import com.streamline.tv.data.weather.GeoLocation
import com.streamline.tv.data.weather.WeatherSnapshot
import java.time.LocalDateTime
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeUiState(
    val settings: AppSettings = AppSettings(),
    val apps: List<AppEntry> = emptyList(),
    val weather: WeatherSnapshot? = null,
    val weatherLoading: Boolean = true,
    val weatherError: Boolean = false,
    val now: LocalDateTime = LocalDateTime.now(),
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as StreamlineApplication).container
    private val settingsRepository = container.settingsRepository

    private val allApps = MutableStateFlow<List<AppEntry>>(emptyList())
    private val weatherState = MutableStateFlow(WeatherUiState())
    private val clock = MutableStateFlow(LocalDateTime.now())
    private val _statusMessage = MutableStateFlow<String?>(null)

    val installedApps: StateFlow<List<AppEntry>> = allApps.asStateFlow()
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.settings,
        allApps,
        weatherState,
        clock,
    ) { currentSettings, apps, weather, now ->
        HomeUiState(
            settings = currentSettings,
            apps = visibleApps(apps, currentSettings),
            weather = weather.snapshot,
            weatherLoading = weather.loading,
            weatherError = weather.error,
            now = now,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch { settingsRepository.ensureDefaultFavorites() }
        refreshApps()
        startClock()
        observeWeather()
        autoDetectLocationIfEnabled()
    }

    fun refreshApps() {
        viewModelScope.launch {
            allApps.value = container.appsRepository.loadInstalledApps()
        }
    }

    fun launchApp(packageName: String): Boolean = container.appsRepository.launch(packageName)

    fun exitToFireTvHome(): Boolean = container.appsRepository.launchFireTvHome()

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }

    fun detectLocation() {
        viewModelScope.launch {
            _statusMessage.value = "Detecting location…"
            val detected = container.locationResolver.detect()
            if (detected != null) {
                settingsRepository.update {
                    it.copy(
                        autoDetectLocation = true,
                        locationLabel = detected.label,
                        latitude = detected.latitude,
                        longitude = detected.longitude,
                    )
                }
                _statusMessage.value = "Location set to ${detected.label}"
            } else {
                _statusMessage.value = "Could not detect a location"
            }
            delay(4_000)
            _statusMessage.value = null
        }
    }

    fun setLocation(location: GeoLocation) {
        viewModelScope.launch {
            settingsRepository.update {
                it.copy(
                    autoDetectLocation = false,
                    locationLabel = location.label,
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            }
            _statusMessage.value = "Location set to ${location.label}"
            delay(4_000)
            _statusMessage.value = null
        }
    }

    fun refreshWeatherNow() {
        viewModelScope.launch {
            val current = settingsRepository.settings.first()
            loadWeather(current)
        }
    }

    suspend fun searchLocations(query: String): List<GeoLocation> =
        container.weatherRepository.searchLocations(query)

    private fun startClock() {
        viewModelScope.launch {
            while (isActive) {
                clock.value = LocalDateTime.now()
                delay(1_000L - (System.currentTimeMillis() % 1_000L))
            }
        }
    }

    private fun observeWeather() {
        viewModelScope.launch {
            settingsRepository.settings
                .map { WeatherKey(it.latitude, it.longitude, it.temperatureUnit, it.locationLabel) }
                .distinctUntilChanged()
                .collectLatest { key ->
                    while (isActive) {
                        loadWeather(key)
                        delay(WEATHER_REFRESH_INTERVAL_MS)
                    }
                }
        }
    }

    private suspend fun loadWeather(key: WeatherKey) {
        weatherState.update { it.copy(loading = true, error = false) }
        val result = withContext(Dispatchers.IO) {
            container.weatherRepository.fetch(
                GeoLocation(key.label, key.latitude, key.longitude),
                key.unit,
            )
        }
        result
            .onSuccess { snapshot -> weatherState.value = WeatherUiState(snapshot = snapshot) }
            .onFailure { weatherState.update { it.copy(loading = false, error = true) } }
    }

    private fun loadWeather(settings: AppSettings) {
        viewModelScope.launch {
            loadWeather(WeatherKey(settings.latitude, settings.longitude, settings.temperatureUnit, settings.locationLabel))
        }
    }

    private fun autoDetectLocationIfEnabled() {
        viewModelScope.launch {
            val current = settingsRepository.settings.first()
            if (!current.autoDetectLocation) return@launch
            val detected = container.locationResolver.detect() ?: return@launch
            settingsRepository.update {
                it.copy(
                    locationLabel = detected.label,
                    latitude = detected.latitude,
                    longitude = detected.longitude,
                )
            }
        }
    }

    private fun visibleApps(apps: List<AppEntry>, settings: AppSettings): List<AppEntry> {
        val available = apps
            .filterNot { settings.isHidden(it.packageName) }
            .let { if (settings.showAllApps) it else it.filter { app -> settings.isFavorite(app.packageName) } }
        val installed = available.map { it.packageName }.toSet()
        val defaultRanks = DEFAULT_APP_GROUPS.mapIndexedNotNull { index, candidates ->
            candidates
                .firstOrNull { it in installed && settings.isFavorite(it) }
                ?.let { it to index }
        }.toMap()

        return available.sortedWith(
            compareBy<AppEntry> { defaultRanks[it.packageName] ?: defaultTailRank(it, settings) }
                .thenBy { it.label.lowercase(Locale.getDefault()) },
        )
    }

    private fun defaultTailRank(app: AppEntry, settings: AppSettings): Int =
        DEFAULT_APP_GROUPS.size + if (settings.isFavorite(app.packageName)) 0 else 1

    private data class WeatherKey(
        val latitude: Double,
        val longitude: Double,
        val unit: com.streamline.tv.data.TemperatureUnit,
        val label: String,
    )

    private data class WeatherUiState(
        val snapshot: WeatherSnapshot? = null,
        val loading: Boolean = true,
        val error: Boolean = false,
    )

    private companion object {
        const val WEATHER_REFRESH_INTERVAL_MS = 15 * 60 * 1000L
    }
}
