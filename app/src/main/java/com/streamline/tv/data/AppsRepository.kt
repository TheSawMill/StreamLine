package com.streamline.tv.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.streamline.tv.R
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?,
    val isSystem: Boolean,
)

/**
 * Discovers launchable apps and launches them. Uses both LAUNCHER and
 * LEANBACK_LAUNCHER categories because Fire TV apps frequently declare only the
 * TV category.
 */
class AppsRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    @Volatile private var launchTargets: Map<String, LaunchTarget> = emptyMap()

    suspend fun loadInstalledApps(): List<AppEntry> = withContext(Dispatchers.IO) {
        val resolved = LinkedHashMap<String, Pair<ResolveInfo, String>>()

        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val tvIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)

        // Query the normal launcher first so it wins for apps that declare both.
        listOf(
            launcherIntent to Intent.CATEGORY_LAUNCHER,
            tvIntent to Intent.CATEGORY_LEANBACK_LAUNCHER,
        ).forEach { (intent, category) ->
            packageManager.queryIntentActivities(intent, 0).forEach { info ->
                val packageName = info.activityInfo.packageName
                if (packageName != context.packageName && packageName !in resolved) {
                    resolved[packageName] = info to category
                }
            }
        }

        val targets = HashMap<String, LaunchTarget>(resolved.size)
        val apps = resolved.mapNotNull { (packageName, entry) ->
            val (resolveInfo, category) = entry
            val appInfo = runCatching { packageManager.getApplicationInfo(packageName, 0) }
                .getOrNull() ?: return@mapNotNull null
            targets[packageName] = LaunchTarget(
                component = ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name),
                category = category,
            )
            AppEntry(
                packageName = packageName,
                label = displayLabel(
                    packageName,
                    runCatching { packageManager.getApplicationLabel(appInfo).toString() }
                        .getOrDefault(packageName),
                ),
                icon = loadIcon(packageName),
                isSystem = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0,
            )
        }
        // Publish only after the whole map is built so a concurrent launch
        // never sees a half-populated set of targets.
        launchTargets = targets
        apps.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    fun launch(packageName: String): Boolean {
        // getLaunchIntentForPackage() only resolves CATEGORY_LAUNCHER activities.
        // Many TV apps declare only CATEGORY_LEANBACK_LAUNCHER, so use the
        // component recorded during discovery as a fallback.
        val intent = packageManager.getLaunchIntentForPackage(packageName)
            ?: launchTargets[packageName]?.let { target ->
                Intent(Intent.ACTION_MAIN)
                    .addCategory(target.category)
                    .setComponent(target.component)
            }
            ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    /**
     * Opens the stock Fire TV launcher even when StreamLine is the default HOME
     * app, so a generic HOME intent would resolve back to us.
     */
    fun launchFireTvHome(): Boolean {
        val flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val amazonHome = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .setComponent(
                ComponentName(
                    "com.amazon.tv.launcher",
                    "com.amazon.tv.launcher.ui.HomeActivity_vNext",
                ),
            )
            .addFlags(flags)
        if (start(amazonHome)) return true

        packageManager.getLaunchIntentForPackage("com.amazon.tv.launcher")
            ?.addFlags(flags)
            ?.let { if (start(it)) return true }

        val fallback = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .addFlags(flags)
        return start(fallback)
    }

    private fun start(intent: Intent): Boolean = runCatching {
        context.startActivity(intent)
        true
    }.getOrDefault(false)

    private data class LaunchTarget(val component: ComponentName, val category: String)

    private fun loadIcon(packageName: String): ImageBitmap? = runCatching {
        // Bundled glyphs keep the four default tiles visually consistent; other
        // apps keep their own icons so the rest of the list stays recognisable.
        val drawable = themedIcon(packageName) ?: packageManager.getApplicationIcon(packageName)
        val size = ICON_SIZE_PX
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        bitmap.asImageBitmap()
    }.getOrNull()

    private fun themedIcon(packageName: String) = when (packageName) {
        "com.plexapp.android" -> R.drawable.ic_tile_plex
        "com.google.android.youtube.tv",
        "com.google.android.youtube",
        "com.amazon.firetv.youtube",
        -> R.drawable.ic_tile_youtube
        "com.google.android.apps.youtube.unplugged",
        "com.amazon.firetv.youtube.tv",
        -> R.drawable.ic_tile_youtube_tv
        "com.bamnetworks.mobile.android.gameday.atbat",
        in MLB_PACKAGE_CANDIDATES -> R.drawable.ic_tile_mlb
        else -> 0
    }.let { resId ->
        if (resId == 0) null else runCatching { context.getDrawable(resId) }.getOrNull()
    }

    /** The Fire TV app reports itself as "MLB"; keep the tile label consistent. */
    private fun displayLabel(packageName: String, label: String): String =
        if (packageName in MLB_PACKAGE_CANDIDATES) "MLB TV" else label

    private companion object {
        const val ICON_SIZE_PX = 144
    }
}
