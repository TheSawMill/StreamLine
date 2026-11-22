package com.streamline.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamline.tv.ui.theme.StreamlineColors

@Composable
fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val glow by animateFloatAsState(if (focused) 1f else 0f, tween(160), label = "settingsGlow")

    Row(
        modifier = modifier
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.06f + 0.06f * glow))
            .border(
                width = 1.dp,
                color = if (focused) StreamlineColors.Mint.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(50),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        GearIcon(color = if (focused) StreamlineColors.Mint else StreamlineColors.Cloud.copy(alpha = 0.85f))
        Spacer(Modifier.width(10.dp))
        BasicText(
            text = "Settings",
            style = TextStyle(
                color = if (focused) StreamlineColors.Cloud else StreamlineColors.Cloud.copy(alpha = 0.80f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.4.sp,
            ),
        )
    }
}

@Composable
private fun GearIcon(color: Color) {
    Canvas(Modifier.size(20.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.26f
        repeat(8) { index ->
            rotate(degrees = index * 45f, pivot = center) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(center.x - radius * 0.20f, center.y - radius * 1.62f),
                    size = Size(radius * 0.40f, radius * 0.62f),
                    cornerRadius = CornerRadius(radius * 0.12f),
                )
            }
        }
        drawCircle(color = color, radius = radius, center = center, style = Stroke(width = radius * 0.46f))
    }
}

@Composable
fun ActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    focusRequester: FocusRequester? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.05f else 1f, tween(150), label = "buttonScale")
    val shape = RoundedCornerShape(50)
    val background = when {
        primary && focused -> StreamlineColors.Mint
        primary -> StreamlineColors.Mint.copy(alpha = 0.85f)
        focused -> Color.White.copy(alpha = 0.16f)
        else -> Color.White.copy(alpha = 0.07f)
    }
    val contentColor = when {
        primary -> StreamlineColors.Ink
        focused -> StreamlineColors.Cloud
        else -> StreamlineColors.Cloud.copy(alpha = 0.85f)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clip(shape)
            .background(background)
            .border(
                width = 1.dp,
                color = if (focused && !primary) StreamlineColors.Mint.copy(alpha = 0.75f) else Color.Transparent,
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 30.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = contentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp,
            ),
        )
    }
}

@Composable
fun ToggleChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(50)
    val background = when {
        active && focused -> StreamlineColors.Mint
        active -> StreamlineColors.Mint.copy(alpha = 0.22f)
        focused -> Color.White.copy(alpha = 0.16f)
        else -> Color.White.copy(alpha = 0.06f)
    }
    val contentColor = when {
        active && focused -> StreamlineColors.Ink
        active -> StreamlineColors.Mint
        focused -> StreamlineColors.Cloud
        else -> StreamlineColors.Cloud.copy(alpha = 0.70f)
    }

    Box(
        modifier = modifier
            .onFocusChanged { focused = it.isFocused }
            .clip(shape)
            .background(background)
            .border(
                width = 1.dp,
                color = when {
                    focused -> StreamlineColors.Mint.copy(alpha = 0.8f)
                    active -> StreamlineColors.Mint.copy(alpha = 0.35f)
                    else -> Color.White.copy(alpha = 0.10f)
                },
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp,
            ),
        )
    }
}

@Composable
fun FocusDot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(StreamlineColors.Mint.copy(alpha = 0.9f)),
    )
}

private val UnusedCap = StrokeCap.Round
