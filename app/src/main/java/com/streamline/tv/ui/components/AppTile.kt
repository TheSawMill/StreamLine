package com.streamline.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamline.tv.data.AppEntry
import com.streamline.tv.ui.theme.StreamlineColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTile(
    app: AppEntry,
    showLabel: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tileSize: Dp = 128.dp,
    focusRequester: FocusRequester? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.09f else 1f,
        animationSpec = tween(durationMillis = 170),
        label = "tileScale",
    )
    val glow by animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = tween(durationMillis = 170),
        label = "tileGlow",
    )
    val bringIntoView = remember { BringIntoViewRequester() }
    val shape = RoundedCornerShape(tileSize * 0.23f)

    LaunchedEffect(focused) {
        if (focused) runCatching { bringIntoView.bringIntoView() }
    }

    Column(
        modifier = modifier
            .width(tileSize)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(tileSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .onFocusChanged { focused = it.isFocused }
                .bringIntoViewRequester(bringIntoView)
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.13f + 0.05f * glow),
                            Color.White.copy(alpha = 0.05f),
                        ),
                    ),
                )
                .border(
                    width = if (glow > 0.5f) 2.dp else 1.dp,
                    color = if (glow > 0f) {
                        StreamlineColors.Mint.copy(alpha = 0.25f + 0.65f * glow)
                    } else {
                        Color.White.copy(alpha = 0.12f)
                    },
                    shape = shape,
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (app.icon != null) {
                Image(
                    bitmap = app.icon,
                    contentDescription = app.label,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(tileSize * 0.16f),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(tileSize * 0.55f)
                        .clip(RoundedCornerShape(50))
                        .background(StreamlineColors.Mint.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = app.label.take(1).uppercase(),
                        style = TextStyle(
                            color = StreamlineColors.Mint,
                            fontSize = (tileSize.value * 0.26f).sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
        }

        if (showLabel) {
            BasicText(
                text = app.label,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 2.dp, end = 2.dp),
                style = TextStyle(
                    color = Color.White.copy(alpha = if (focused) 1f else 0.72f),
                    fontSize = 13.sp,
                    fontWeight = if (focused) FontWeight.SemiBold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
