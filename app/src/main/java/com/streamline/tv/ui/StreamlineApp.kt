package com.streamline.tv.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.streamline.tv.R
import com.streamline.tv.ui.components.ExitDialog
import com.streamline.tv.ui.home.HomeScreen
import com.streamline.tv.ui.home.HomeViewModel
import com.streamline.tv.ui.settings.ManageAppsScreen
import com.streamline.tv.ui.settings.SettingsScreen

private enum class Screen { HOME, SETTINGS, MANAGE_APPS }

@Composable
fun StreamlineApp(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = screen != Screen.HOME) { screen = Screen.HOME }
    BackHandler(enabled = screen == Screen.HOME) { showExitDialog = true }

    when (screen) {
        Screen.HOME -> HomeScreen(
            state = state,
            onLaunchApp = { app ->
                if (!viewModel.launchApp(app.packageName)) {
                    Toast.makeText(context, R.string.launch_failed, Toast.LENGTH_SHORT).show()
                }
            },
            onOpenSettings = { screen = Screen.SETTINGS },
        )

        Screen.SETTINGS -> SettingsScreen(
            settings = settings,
            statusMessage = statusMessage,
            installedAppCount = installedApps.size,
            onBack = { screen = Screen.HOME },
            onUpdate = viewModel::updateSettings,
            onManageApps = { screen = Screen.MANAGE_APPS },
            onDetectLocation = viewModel::detectLocation,
            onRefreshWeather = viewModel::refreshWeatherNow,
            onSetLocation = viewModel::setLocation,
            searchLocations = { query -> viewModel.searchLocations(query) },
        )

        Screen.MANAGE_APPS -> ManageAppsScreen(
            apps = installedApps,
            settings = settings,
            onBack = { screen = Screen.SETTINGS },
            onToggleFavorite = { app, favorite ->
                viewModel.updateSettings { current ->
                    current.copy(
                        favorites = if (favorite) current.favorites + app.packageName
                        else current.favorites - app.packageName,
                    )
                }
            },
            onToggleHidden = { app, hidden ->
                viewModel.updateSettings { current ->
                    current.copy(
                        hiddenApps = if (hidden) current.hiddenApps + app.packageName
                        else current.hiddenApps - app.packageName,
                    )
                }
            },
        )
    }

    if (showExitDialog) {
        ExitDialog(
            onDismiss = { showExitDialog = false },
            onExit = {
                showExitDialog = false
                if (viewModel.exitToFireTvHome()) {
                    (context as? Activity)?.finish()
                } else {
                    Toast.makeText(context, R.string.exit_failed, Toast.LENGTH_SHORT).show()
                }
            },
        )
    }
}
