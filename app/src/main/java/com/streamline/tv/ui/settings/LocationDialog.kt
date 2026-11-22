package com.streamline.tv.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.streamline.tv.data.weather.GeoLocation
import com.streamline.tv.ui.components.glassSurface
import com.streamline.tv.ui.theme.StreamlineColors
import kotlinx.coroutines.delay

@Composable
fun LocationDialog(
    currentLabel: String,
    searchLocations: suspend (String) -> List<GeoLocation>,
    onSelect: (GeoLocation) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<GeoLocation>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    val fieldFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) { runCatching { fieldFocus.requestFocus() } }

    LaunchedEffect(query) {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            results = emptyList()
            searching = false
            return@LaunchedEffect
        }
        searching = true
        delay(350)
        results = searchLocations(trimmed)
        searching = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.68f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(720.dp)
                    .glassSurface(shape = RoundedCornerShape(26.dp), alpha = 0.12f, borderAlpha = 0.20f)
                    .padding(30.dp),
            ) {
                BasicText(
                    text = "Choose your location",
                    style = TextStyle(color = StreamlineColors.Cloud, fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
                )
                BasicText(
                    text = "Currently using $currentLabel",
                    modifier = Modifier.padding(top = 4.dp),
                    style = TextStyle(color = StreamlineColors.Mist, fontSize = 13.sp),
                )

                Spacer(Modifier.height(20.dp))
                SearchField(
                    query = query,
                    onQueryChange = { query = it },
                    focusRequester = fieldFocus,
                )
                Spacer(Modifier.height(8.dp))
                BasicText(
                    text = "Type a city, then press OK to search. Results update as you type.",
                    style = TextStyle(color = StreamlineColors.Mist.copy(alpha = 0.7f), fontSize = 11.sp),
                )

                Spacer(Modifier.height(16.dp))

                when {
                    searching -> BasicText(
                        text = "Searching…",
                        style = TextStyle(color = StreamlineColors.Mint, fontSize = 13.sp),
                    )

                    results.isEmpty() && query.trim().length >= 2 -> BasicText(
                        text = "No matches. Try a nearby larger city.",
                        style = TextStyle(color = StreamlineColors.Mist, fontSize = 13.sp),
                    )

                    else -> LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(results, key = { "${it.latitude},${it.longitude},${it.label}" }) { location ->
                            LocationRow(location = location, onClick = { onSelect(location) })
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    androidx.compose.foundation.text.BasicText(
                        text = "Press BACK to cancel",
                        style = TextStyle(color = StreamlineColors.Mist.copy(alpha = 0.7f), fontSize = 11.sp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
) {
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(14.dp)

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused }
            .clip(shape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(
                width = 1.dp,
                color = if (focused) StreamlineColors.Mint.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.10f),
                shape = shape,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        textStyle = TextStyle(color = StreamlineColors.Cloud, fontSize = 17.sp),
        cursorBrush = SolidColor(StreamlineColors.Mint),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        decorationBox = { innerTextField ->
            if (query.isEmpty()) {
                BasicText(
                    text = "Search city…",
                    style = TextStyle(color = StreamlineColors.Mist.copy(alpha = 0.7f), fontSize = 17.sp),
                )
            }
            innerTextField()
        },
    )
}

@Composable
private fun LocationRow(location: GeoLocation, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused }
            .clip(shape)
            .background(if (focused) StreamlineColors.Mint.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f))
            .border(
                width = 1.dp,
                color = if (focused) StreamlineColors.Mint.copy(alpha = 0.65f) else Color.Transparent,
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = location.label,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                color = if (focused) StreamlineColors.Cloud else StreamlineColors.Cloud.copy(alpha = 0.85f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        BasicText(
            text = "${"%.2f".format(location.latitude)}, ${"%.2f".format(location.longitude)}",
            style = TextStyle(color = StreamlineColors.Mist.copy(alpha = 0.7f), fontSize = 11.sp),
        )
    }
}
