package com.streamline.tv.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamline.tv.ui.components.SectionLabel
import com.streamline.tv.ui.theme.StreamlineColors

@Composable
fun SettingsSection(title: String, modifier: Modifier = Modifier) {
    SectionLabel(
        text = title,
        modifier = modifier.padding(start = 8.dp, top = 22.dp, bottom = 6.dp),
    )
}

@Composable
fun SettingRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    showChevron: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    focusRequester: FocusRequester? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clip(shape)
            .background(if (focused) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.035f))
            .border(
                width = 1.dp,
                color = if (focused) StreamlineColors.Mint.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.06f),
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(50))
                .background(if (focused) StreamlineColors.Mint else Color.Transparent),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = TextStyle(
                    color = StreamlineColors.Cloud.copy(alpha = if (focused) 1f else 0.92f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            if (subtitle != null) {
                BasicText(
                    text = subtitle,
                    modifier = Modifier.padding(top = 2.dp),
                    style = TextStyle(color = StreamlineColors.Mist, fontSize = 12.sp, lineHeight = 16.sp),
                )
            }
        }
        if (value != null) {
            BasicText(
                text = value,
                style = TextStyle(
                    color = if (focused) StreamlineColors.Mint else StreamlineColors.Mist,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
        if (showChevron) {
            BasicText(
                text = "›",
                modifier = Modifier.padding(start = 12.dp),
                style = TextStyle(color = StreamlineColors.Mist.copy(alpha = 0.7f), fontSize = 22.sp),
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun SwitchIndicator(active: Boolean) {
    val background by animateColorAsState(
        targetValue = if (active) StreamlineColors.Mint else Color.White.copy(alpha = 0.16f),
        animationSpec = tween(160),
        label = "switchBackground",
    )
    val offset by animateDpAsState(
        targetValue = if (active) 21.dp else 3.dp,
        animationSpec = tween(160),
        label = "switchOffset",
    )

    Box(
        modifier = Modifier
            .padding(start = 12.dp)
            .width(42.dp)
            .height(22.dp)
            .clip(RoundedCornerShape(50))
            .background(background),
    ) {
        Box(
            modifier = Modifier
                .offset(x = offset, y = 3.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(if (active) StreamlineColors.Ink else Color.White.copy(alpha = 0.75f)),
        )
    }
}

@Composable
fun InfoCard(title: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
    ) {
        BasicText(
            text = title,
            style = TextStyle(color = StreamlineColors.Cloud.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
        )
        BasicText(
            text = body,
            style = TextStyle(color = StreamlineColors.Mist.copy(alpha = 0.85f), fontSize = 12.sp, lineHeight = 17.sp),
        )
    }
}
