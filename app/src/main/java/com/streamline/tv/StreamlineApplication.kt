package com.streamline.tv

import android.app.Application
import android.content.Context
import com.streamline.tv.data.AppsRepository
import com.streamline.tv.data.SettingsRepository
import com.streamline.tv.data.weather.LocationResolver
import com.streamline.tv.data.weather.WeatherRepository

class StreamlineApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/**
 * Tiny manual DI container. Everything is stateless or DataStore-backed, so a
 * single instance per process is enough and keeps startup cheap.
 */
class AppContainer(context: Context) {
    val settingsRepository = SettingsRepository(context)
    val appsRepository = AppsRepository(context)
    val weatherRepository = WeatherRepository()
    val locationResolver = LocationResolver()
}
