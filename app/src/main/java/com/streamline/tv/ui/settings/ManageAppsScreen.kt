package com.streamline.tv.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamline.tv.data.AppEntry
import com.streamline.tv.data.AppSettings
import com.streamline.tv.ui.components.ActionButton
import com.streamline.tv.ui.components.FocusOnEnter
import com.streamline.tv.ui.components.ToggleChip
import com.streamline.tv.ui.theme.StreamlineColors

@Composable
fun ManageAppsScreen(
    apps: List<AppEntry>,
    settings: AppSettings,
    onBack: () -> Unit,
    onToggleFavorite: (AppEntry, Boolean) -> Unit,
    onToggleHidden: (AppEntry, Boolean) -> Unit,
) {
    val firstChipFocus = remember { FocusRequester() }

    FocusOnEnter(focusRequester = firstChipFocus)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to StreamlineColors.Ink,
                    0.55f to StreamlineColors.InkElevated,
                    1f to StreamlineColors.Ink,
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 72.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 1100.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionButton(label = "‹  Back", onClick = onBack)
                    Spacer(Modifier.width(22.dp))
                    Column {
                        BasicText(
                            text = "Manage apps",
                            style = TextStyle(color = StreamlineColors.Cloud, fontSize = 30.sp, fontWeight = FontWeight.SemiBold),
                        )
                        BasicText(
                            text = "Favourites are pinned first. Hidden apps stay installed but leave the home screen.",
                            style = TextStyle(color = StreamlineColors.Mist, fontSize = 13.sp),
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                if (apps.isEmpty()) {
                    BasicText(
                        text = "No launchable apps found.",
                        style = TextStyle(color = StreamlineColors.Mist, fontSize = 15.sp),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
                            AppManagementRow(
                                app = app,
                                favorite = settings.isFavorite(app.packageName),
                                hidden = settings.isHidden(app.packageName),
                                firstChipFocus = if (index == 0) firstChipFocus else null,
                                onToggleFavorite = { onToggleFavorite(app, !settings.isFavorite(app.packageName)) },
                                onToggleHidden = { onToggleHidden(app, !settings.isHidden(app.packageName)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppManagementRow(
    app: AppEntry,
    favorite: Boolean,
    hidden: Boolean,
    firstChipFocus: FocusRequester?,
    onToggleFavorite: () -> Unit,
    onToggleHidden: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = if (hidden) 0.02f else 0.04f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.06f)),
            contentAlignment = Alignment.Center,
        ) {
            if (app.icon != null) {
                Image(
                    bitmap = app.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                BasicText(
                    text = app.label.take(1).uppercase(),
                    style = TextStyle(color = StreamlineColors.Mint, fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = app.label,
                style = TextStyle(
                    color = StreamlineColors.Cloud.copy(alpha = if (hidden) 0.55f else 0.95f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                text = app.packageName,
                style = TextStyle(color = StreamlineColors.Mist.copy(alpha = 0.65f), fontSize = 11.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        ToggleChip(
            label = if (favorite) "★ Favourite" else "☆ Favourite",
            active = favorite,
            onClick = onToggleFavorite,
            modifier = if (firstChipFocus != null) Modifier.focusRequester(firstChipFocus) else Modifier,
        )
        Spacer(Modifier.width(8.dp))
        ToggleChip(
            label = if (hidden) "Hidden" else "Visible",
            active = hidden,
            onClick = onToggleHidden,
        )
    }
}
