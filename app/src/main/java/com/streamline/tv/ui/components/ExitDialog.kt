package com.streamline.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.streamline.tv.ui.theme.StreamlineColors

@Composable
fun ExitDialog(
    onDismiss: () -> Unit,
    onExit: () -> Unit,
) {
    val cancelFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { cancelFocus.requestFocus() } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.66f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(560.dp)
                    .glassSurface(shape = RoundedCornerShape(28.dp), alpha = 0.13f, borderAlpha = 0.20f)
                    .padding(34.dp),
            ) {
                BasicText(
                    text = "Exit to Fire TV?",
                    style = TextStyle(color = StreamlineColors.Cloud, fontSize = 26.sp, fontWeight = FontWeight.SemiBold),
                )
                Spacer(Modifier.height(8.dp))
                BasicText(
                    text = "Amazon's Fire TV home screen will open. Your apps and settings stay exactly as they are.",
                    style = TextStyle(color = StreamlineColors.Mist, fontSize = 14.sp, lineHeight = 20.sp),
                )
                Spacer(Modifier.height(26.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionButton(
                        label = "Stay",
                        onClick = onDismiss,
                        primary = true,
                        focusRequester = cancelFocus,
                    )
                    ActionButton(label = "Exit", onClick = onExit)
                }
            }
        }
    }
}
