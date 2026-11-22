package com.streamline.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import com.streamline.tv.ui.theme.StreamlineColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TimeShadow = Shadow(
    color = Color.Black.copy(alpha = 0.45f),
    offset = Offset(0f, 4f),
    blurRadius = 18f,
)

@Composable
fun ClockBlock(
    now: LocalDateTime,
    use24HourClock: Boolean,
    showSeconds: Boolean,
    modifier: Modifier = Modifier,
) {
    val timePattern = when {
        use24HourClock && showSeconds -> "HH:mm:ss"
        use24HourClock -> "HH:mm"
        showSeconds -> "h:mm:ss"
        else -> "h:mm"
    }
    val timeText = now.format(DateTimeFormatter.ofPattern(timePattern, Locale.getDefault()))
    val meridiem = if (use24HourClock) null else now.format(DateTimeFormatter.ofPattern("a", Locale.getDefault()))
    val dateText = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))

    androidx.compose.foundation.layout.Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            androidx.compose.foundation.text.BasicText(
                text = timeText,
                style = TextStyle(
                    color = StreamlineColors.Cloud,
                    fontSize = 86.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1.5).sp,
                    fontFeatureSettings = "tnum",
                    shadow = TimeShadow,
                ),
            )
            if (meridiem != null) {
                androidx.compose.foundation.text.BasicText(
                    text = meridiem,
                    modifier = Modifier.padding(start = 10.dp, bottom = 20.dp),
                    style = TextStyle(
                        color = StreamlineColors.Cloud.copy(alpha = 0.72f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        shadow = TimeShadow,
                    ),
                )
            }
        }
        androidx.compose.foundation.text.BasicText(
            text = dateText,
            modifier = Modifier.padding(top = 2.dp),
            style = TextStyle(
                color = StreamlineColors.Cloud.copy(alpha = 0.70f),
                fontSize = 19.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp,
                shadow = TimeShadow,
            ),
        )
    }
}

/** Shared translucent "glass" treatment used by widgets and dialogs. */
fun Modifier.glassSurface(
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    tint: Color = Color.White,
    alpha: Float = 0.08f,
    borderAlpha: Float = 0.14f,
): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(tint.copy(alpha = alpha), tint.copy(alpha = alpha * 0.45f)),
        ),
    )
    .border(1.dp, tint.copy(alpha = borderAlpha), shape)

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    androidx.compose.foundation.text.BasicText(
        text = text.uppercase(Locale.getDefault()),
        modifier = modifier,
        style = TextStyle(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.4.sp,
        ),
    )
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.glassSurface(shape = RoundedCornerShape(26.dp), alpha = 0.07f),
    ) {
        content()
    }
}
