package com.streamline.tv.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamline.tv.BuildConfig
import com.streamline.tv.data.AppSettings
import com.streamline.tv.data.BackgroundStyle
import com.streamline.tv.data.LayoutMode
import com.streamline.tv.data.TemperatureUnit
import com.streamline.tv.data.TimeOfDayMode
import com.streamline.tv.data.weather.GeoLocation
import com.streamline.tv.ui.components.ActionButton
import com.streamline.tv.ui.components.FocusOnEnter
import com.streamline.tv.ui.theme.StreamlineColors

@Composable
fun SettingsScreen(
    settings: AppSettings,
    statusMessage: String?,
    installedAppCount: Int,
    onBack: () -> Unit,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    onManageApps: () -> Unit,
    onDetectLocation: () -> Unit,
    onRefreshWeather: () -> Unit,
    onSetLocation: (GeoLocation) -> Unit,
    searchLocations: suspend (String) -> List<GeoLocation>,
) {
    var showLocationDialog by remember { mutableStateOf(false) }
    val firstRowFocus = remember { FocusRequester() }

    FocusOnEnter(focusRequester = firstRowFocus)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to StreamlineColors.Ink,
                    0.55f to StreamlineColors.InkElevated,
                    1f to StreamlineColors.Ink,
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(StreamlineColors.Mint.copy(alpha = 0.10f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(0f, 0f),
                        radius = 900f,
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 72.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 1100.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionButton(label = "‹  Back", onClick = onBack)
                    Spacer(Modifier.width(22.dp))
                    Column {
                        BasicText(
                            text = "Settings",
                            style = TextStyle(color = StreamlineColors.Cloud, fontSize = 30.sp, fontWeight = FontWeight.SemiBold),
                        )
                        BasicText(
                            text = "Make StreamLine yours",
                            style = TextStyle(color = StreamlineColors.Mist, fontSize = 13.sp),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (statusMessage != null) {
                        BasicText(
                            text = statusMessage,
                            style = TextStyle(color = StreamlineColors.Mint, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    item { SettingsSection("Appearance") }
                    item {
                        SettingRow(
                            title = "Background scene",
                            subtitle = "Photographic nature scenes, no network needed",
                            value = settings.backgroundStyle.label,
                            focusRequester = firstRowFocus,
                            onClick = {
                                onUpdate { it.copy(backgroundStyle = nextEnum(it.backgroundStyle, BackgroundStyle.entries)) }
                            },
                        )
                    }
                    item {
                        SettingRow(
                            title = "Time of day",
                            subtitle = "Auto follows your system clock",
                            value = settings.timeOfDayMode.label,
                            onClick = {
                                onUpdate { it.copy(timeOfDayMode = nextEnum(it.timeOfDayMode, TimeOfDayMode.entries)) }
                            },
                        )
                    }
                    item {
                        SettingRow(
                            title = "Layout",
                            subtitle = "How apps are arranged on the home screen",
                            value = settings.layoutMode.label,
                            onClick = {
                                onUpdate { it.copy(layoutMode = nextEnum(it.layoutMode, LayoutMode.entries)) }
                            },
                        )
                    }
                    item {
                        SettingRow(
                            title = "Reduced motion",
                            subtitle = "Freeze background animation for lower power use",
                            showChevron = false,
                            trailing = { SwitchIndicator(settings.reducedMotion) },
                            onClick = { onUpdate { it.copy(reducedMotion = !it.reducedMotion) } },
                        )
                    }
                    item {
                        SettingRow(
                            title = "App labels",
                            subtitle = "Show the name under each app tile",
                            showChevron = false,
                            trailing = { SwitchIndicator(settings.showAppLabels) },
                            onClick = { onUpdate { it.copy(showAppLabels = !it.showAppLabels) } },
                        )
                    }
                    if (settings.backgroundStyle == BackgroundStyle.VIDEO_WALLPAPER) {
                        item {
                            InfoCard(
                                title = "Using a video wallpaper",
                                body = "Push a looping MP4 to /data/data/com.streamline.tv/files/wallpapers/background.mp4 with adb, " +
                                    "or add res/raw/wallpaper.mp4 to the project. If no video is found, Aurora Peaks is used instead.",
                            )
                        }
                    }

                    item { SettingsSection("Clock & weather") }
                    item {
                        SettingRow(
                            title = "24-hour clock",
                            showChevron = false,
                            trailing = { SwitchIndicator(settings.use24HourClock) },
                            onClick = { onUpdate { it.copy(use24HourClock = !it.use24HourClock) } },
                        )
                    }
                    item {
                        SettingRow(
                            title = "Show seconds",
                            showChevron = false,
                            trailing = { SwitchIndicator(settings.showSeconds) },
                            onClick = { onUpdate { it.copy(showSeconds = !it.showSeconds) } },
                        )
                    }
                    item {
                        SettingRow(
                            title = "Weather widget",
                            showChevron = false,
                            trailing = { SwitchIndicator(settings.showWeather) },
                            onClick = { onUpdate { it.copy(showWeather = !it.showWeather) } },
                        )
                    }
                    item {
                        SettingRow(
                            title = "Temperature unit",
                            value = settings.temperatureUnit.label,
                            onClick = {
                                onUpdate { it.copy(temperatureUnit = nextEnum(it.temperatureUnit, TemperatureUnit.entries)) }
                            },
                        )
                    }
                    item {
                        SettingRow(
                            title = "Location",
                            subtitle = "City used for the weather forecast",
                            value = settings.locationLabel,
                            onClick = { showLocationDialog = true },
                        )
                    }
                    item {
                        SettingRow(
                            title = "Detect location automatically",
                            subtitle = "Approximates your area from your public IP address",
                            showChevron = false,
                            trailing = { SwitchIndicator(settings.autoDetectLocation) },
                            onClick = {
                                if (settings.autoDetectLocation) {
                                    onUpdate { it.copy(autoDetectLocation = false) }
                                } else {
                                    onUpdate { it.copy(autoDetectLocation = true) }
                                    onDetectLocation()
                                }
                            },
                        )
                    }
                    item {
                        SettingRow(
                            title = "Refresh now",
                            subtitle = "Weather refreshes every 15 minutes",
                            showChevron = false,
                            onClick = onRefreshWeather,
                        )
                    }

                    item { SettingsSection("Applications") }
                    item {
                        SettingRow(
                            title = "Show all apps",
                            subtitle = "Off shows only favourites on Home",
                            showChevron = false,
                            trailing = { SwitchIndicator(settings.showAllApps) },
                            onClick = { onUpdate { it.copy(showAllApps = !it.showAllApps) } },
                        )
                    }
                    item {
                        SettingRow(
                            title = "Manage apps",
                            subtitle = "$installedAppCount launchable apps found",
                            value = "Favourites & visibility",
                            onClick = onManageApps,
                        )
                    }

                    item { SettingsSection("About") }
                    item {
                        InfoCard(
                            title = "StreamLine TV ${BuildConfig.VERSION_NAME}",
                            body = "Open source under the MIT licence. No ads, no sponsored rows, no analytics. " +
                                "Weather by Open-Meteo. Nature scenes are rendered locally on your device.",
                        )
                    }
                }
            }
        }
    }

    if (showLocationDialog) {
        LocationDialog(
            currentLabel = settings.locationLabel,
            searchLocations = searchLocations,
            onSelect = { location ->
                onSetLocation(location)
                showLocationDialog = false
            },
            onDismiss = { showLocationDialog = false },
        )
    }
}

private fun <T> nextEnum(current: T, entries: List<T>): T where T : Enum<T> =
    entries[(entries.indexOf(current) + 1) % entries.size]
