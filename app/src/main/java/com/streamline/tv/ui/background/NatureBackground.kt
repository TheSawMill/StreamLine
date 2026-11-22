package com.streamline.tv.ui.background

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import com.streamline.tv.R
import com.streamline.tv.data.BackgroundStyle
import com.streamline.tv.data.TimeOfDayMode
import java.io.File
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.isActive

/**
 * Photographic nature background. The images ship with the app, so the launcher
 * stays offline-capable and never waits on a network request to draw its home
 * screen. A slow scale/drift adds life without the cost of video decode.
 */
@Composable
fun NatureBackground(
    style: BackgroundStyle,
    timeOfDayMode: TimeOfDayMode,
    now: LocalDateTime,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val hour = remember(now, timeOfDayMode) {
        when (timeOfDayMode) {
            TimeOfDayMode.AUTO -> now.hour + now.minute / 60.0
            TimeOfDayMode.DAY -> 12.0
            TimeOfDayMode.DUSK -> 19.0
            TimeOfDayMode.NIGHT -> 23.0
        }
    }
    val imageRes = remember(style) { imageForStyle(style) }
    val motion = remember { mutableFloatStateOf(0.5f) }

    LaunchedEffect(reducedMotion) {
        if (reducedMotion) {
            motion.floatValue = 0.5f
            return@LaunchedEffect
        }
        val start = withFrameNanos { it }
        var lastFrame = start
        while (isActive) {
            withFrameNanos { frame ->
                if (frame - lastFrame >= FRAME_INTERVAL_NANOS) {
                    lastFrame = frame
                    val seconds = (frame - start) / 1_000_000_000f
                    motion.floatValue = (sin(seconds * MOTION_SPEED) * 0.5f + 0.5f).toFloat()
                }
            }
        }
    }

    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val t = motion.floatValue
                    val scale = 1.03f + 0.05f * t
                    scaleX = scale
                    scaleY = scale
                    translationX = (0.5f - t) * size.width * 0.02f
                    translationY = (0.5f - t) * size.height * 0.018f
                },
            contentScale = ContentScale.Crop,
        )

        // A light time-of-day wash keeps the clock and weather readable without
        // hiding the photographic detail.
        val tint = remember(hour) { tintForHour(hour) }
        Box(Modifier.fillMaxSize().background(tint.color.copy(alpha = tint.alpha)))

        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.40f),
                    0.28f to Color.Transparent,
                    0.68f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.50f),
                ),
            ),
        )
    }
}

private fun imageForStyle(style: BackgroundStyle) = when (style) {
    BackgroundStyle.AURORA_PEAKS,
    BackgroundStyle.VIDEO_WALLPAPER,
    -> R.drawable.bg_aurora_peaks
    BackgroundStyle.ALPINE_LAKE -> R.drawable.bg_alpine_lake
    BackgroundStyle.COASTAL_DUSK -> R.drawable.bg_coastal_dusk
    BackgroundStyle.FOREST_NIGHT -> R.drawable.bg_forest_night
}

private data class HourTint(val color: Color, val alpha: Float)

private fun tintForHour(hour: Double): HourTint = when {
    hour < 5.5 || hour >= 20.5 -> HourTint(Color(0xFF07132A), 0.26f)
    hour < 8.5 -> HourTint(Color(0xFFFFAE80), 0.14f)
    hour < 17.0 -> HourTint(Color(0xFFDFF1FF), 0.04f)
    hour < 20.5 -> HourTint(Color(0xFFFF8A5C), 0.16f)
    else -> HourTint(Color(0xFF07132A), 0.26f)
}

/** Video wallpaper support. Drop an MP4 at files/wallpapers/background.mp4 or res/raw/wallpaper.mp4. */
object VideoWallpaperSource {
    fun resolve(context: Context): Uri? {
        val rawId = context.resources.getIdentifier("wallpaper", "raw", context.packageName)
        if (rawId != 0) {
            return Uri.parse("android.resource://${context.packageName}/$rawId")
        }
        val file = File(context.filesDir, "wallpapers/background.mp4")
        if (file.isFile && file.length() > 0L) return Uri.fromFile(file)
        return null
    }
}

@Composable
fun VideoWallpaper(uri: Uri, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            VideoView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setVideoURI(uri)
                setOnPreparedListener { player ->
                    player.isLooping = true
                    player.setVolume(0f, 0f)
                    player.setVideoScalingMode(
                        android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING,
                    )
                    start()
                }
                setOnErrorListener { _, _, _ -> true }
            }
        },
        onRelease = { view -> runCatching { view.stopPlayback() } },
    )
}

private const val FRAME_INTERVAL_NANOS = 33_000_000L
private const val MOTION_SPEED = (PI / 45.0).toFloat()
