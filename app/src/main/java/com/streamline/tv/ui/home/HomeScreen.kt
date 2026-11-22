package com.streamline.tv.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.streamline.tv.data.AppEntry
import com.streamline.tv.data.BackgroundStyle
import com.streamline.tv.data.LayoutMode
import com.streamline.tv.ui.background.NatureBackground
import com.streamline.tv.ui.background.VideoWallpaper
import com.streamline.tv.ui.background.VideoWallpaperSource
import com.streamline.tv.ui.components.AppTile
import com.streamline.tv.ui.components.ClockBlock
import com.streamline.tv.ui.components.FocusOnEnter
import com.streamline.tv.ui.components.SectionLabel
import com.streamline.tv.ui.components.SettingsButton
import com.streamline.tv.ui.components.WeatherWidget
import com.streamline.tv.ui.components.glassSurface
import com.streamline.tv.ui.theme.StreamlineColors
import kotlin.math.ceil

@Composable
fun HomeScreen(
    state: HomeUiState,
    onLaunchApp: (AppEntry) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val settings = state.settings
    val context = LocalContext.current
    val firstAppFocus = remember { FocusRequester() }
    val settingsFocus = remember { FocusRequester() }
    var focusEpoch by remember { mutableIntStateOf(0) }

    LifecycleResumeEffect(Unit) {
        focusEpoch += 1
        onPauseOrDispose { }
    }

    val homeFocus = if (state.apps.isNotEmpty()) firstAppFocus else settingsFocus
    FocusOnEnter(
        focusRequester = homeFocus,
        key = listOf(focusEpoch, state.apps, settings.layoutMode),
    )

    val videoUri = remember(settings.backgroundStyle) {
        if (settings.backgroundStyle == BackgroundStyle.VIDEO_WALLPAPER) {
            VideoWallpaperSource.resolve(context)
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StreamlineColors.Ink),
    ) {
        if (videoUri != null) {
            VideoWallpaper(videoUri, Modifier.fillMaxSize())
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.55f),
                            0.45f to Color.Black.copy(alpha = 0.12f),
                            1f to Color.Black.copy(alpha = 0.78f),
                        ),
                    ),
            )
        } else {
            NatureBackground(
                style = if (settings.backgroundStyle == BackgroundStyle.VIDEO_WALLPAPER) {
                    BackgroundStyle.AURORA_PEAKS
                } else {
                    settings.backgroundStyle
                },
                timeOfDayMode = settings.timeOfDayMode,
                now = state.now,
                reducedMotion = settings.reducedMotion,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 54.dp, vertical = 40.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    ClockBlock(
                        now = state.now,
                        use24HourClock = settings.use24HourClock,
                        showSeconds = settings.showSeconds,
                    )
                    if (settings.showWeather) {
                        Spacer(Modifier.height(14.dp))
                        WeatherWidget(
                            weather = state.weather,
                            unit = settings.temperatureUnit,
                            loading = state.weatherLoading,
                        )
                    }
                }
                SettingsButton(
                    onClick = onOpenSettings,
                    focusRequester = if (state.apps.isEmpty()) settingsFocus else null,
                )
            }

            if (settings.layoutMode == LayoutMode.GRID) {
                Spacer(Modifier.height(28.dp))
                AppGrid(
                    apps = state.apps,
                    showLabels = settings.showAppLabels,
                    firstFocus = firstAppFocus,
                    onLaunchApp = onLaunchApp,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(22.dp))
                AppShelf(
                    apps = state.apps,
                    showLabels = settings.showAppLabels,
                    layoutMode = settings.layoutMode,
                    firstFocus = firstAppFocus,
                    onLaunchApp = onLaunchApp,
                )
            }

            FooterHint(Modifier.padding(top = 14.dp))
        }
    }
}

@Composable
private fun AppShelf(
    apps: List<AppEntry>,
    showLabels: Boolean,
    layoutMode: LayoutMode,
    firstFocus: FocusRequester,
    onLaunchApp: (AppEntry) -> Unit,
) {
    if (apps.isEmpty()) {
        EmptyAppsPanel()
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionLabel(
            text = "Your apps",
            modifier = Modifier.padding(start = 14.dp, bottom = 2.dp),
        )
        if (layoutMode == LayoutMode.DOUBLE_ROW) {
            val rowSize = ceil(apps.size / 2.0).toInt().coerceAtLeast(1)
            val rows = apps.chunked(rowSize)
            rows.forEachIndexed { rowIndex, rowApps ->
                AppRow(
                    apps = rowApps,
                    showLabels = showLabels,
                    onLaunchApp = onLaunchApp,
                    firstFocus = if (rowIndex == 0) firstFocus else null,
                )
            }
        } else {
            AppRow(
                apps = apps,
                showLabels = showLabels,
                onLaunchApp = onLaunchApp,
                firstFocus = firstFocus,
            )
        }
    }
}

@Composable
private fun AppRow(
    apps: List<AppEntry>,
    showLabels: Boolean,
    onLaunchApp: (AppEntry) -> Unit,
    firstFocus: FocusRequester?,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
    ) {
        itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
            AppTile(
                app = app,
                showLabel = showLabels,
                onClick = { onLaunchApp(app) },
                focusRequester = if (index == 0) firstFocus else null,
            )
        }
    }
}

@Composable
private fun AppGrid(
    apps: List<AppEntry>,
    showLabels: Boolean,
    firstFocus: FocusRequester,
    onLaunchApp: (AppEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (apps.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            EmptyAppsPanel()
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    ) {
        items(apps, key = { it.packageName }) { app ->
            val isFirst = app == apps.first()
            AppTile(
                app = app,
                showLabel = showLabels,
                onClick = { onLaunchApp(app) },
                tileSize = 112.dp,
                focusRequester = if (isFirst) firstFocus else null,
            )
        }
    }
}

@Composable
private fun EmptyAppsPanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(alpha = 0.06f)
            .padding(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BasicText(
            text = "No apps found",
            style = TextStyle(color = StreamlineColors.Cloud, fontSize = 20.sp, fontWeight = FontWeight.Medium),
        )
        BasicText(
            text = "Install streaming apps from the Amazon Appstore and they will appear here.",
            style = TextStyle(color = StreamlineColors.Mist, fontSize = 14.sp),
        )
    }
}

@Composable
private fun FooterHint(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HintItem("OK", "Open")
        Spacer(Modifier.width(18.dp))
        HintItem("◀ ▶", "Navigate")
        Spacer(Modifier.width(18.dp))
        HintItem("BACK", "Exit")
    }
}

@Composable
private fun HintItem(key: String, action: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .glassSurface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(7.dp),
                    alpha = 0.10f,
                    borderAlpha = 0.18f,
                )
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            BasicText(
                text = key,
                style = TextStyle(
                    color = StreamlineColors.Cloud.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                ),
            )
        }
        Spacer(Modifier.width(7.dp))
        BasicText(
            text = action,
            style = TextStyle(color = StreamlineColors.Mist.copy(alpha = 0.75f), fontSize = 11.sp),
        )
    }
}
