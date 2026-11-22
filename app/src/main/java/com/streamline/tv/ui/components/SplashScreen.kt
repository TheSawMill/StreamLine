package com.streamline.tv.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamline.tv.R
import com.streamline.tv.ui.theme.StreamlineColors

/** Branded loading surface shown while the launcher initialises. */
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        StreamlineColors.Ink,
                        StreamlineColors.InkElevated,
                        StreamlineColors.Ink,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(520.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            StreamlineColors.Mint.copy(alpha = 0.10f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(StreamlineColors.InkGlass, StreamlineColors.InkElevated),
                        ),
                    )
                    .border(2.dp, StreamlineColors.Mint.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "StreamLine TV",
                    modifier = Modifier.size(104.dp),
                )
            }

            Spacer(Modifier.height(22.dp))
            BasicText(
                text = "StreamLine",
                style = TextStyle(
                    color = StreamlineColors.Cloud,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            BasicText(
                text = "TV",
                style = TextStyle(
                    color = StreamlineColors.Amber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(Modifier.height(10.dp))
            BasicText(
                text = "CALM  •  AD-FREE  •  REMOTE-FIRST",
                style = TextStyle(
                    color = StreamlineColors.Mist,
                    fontSize = 12.sp,
                ),
            )
            Spacer(Modifier.height(30.dp))
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                StreamlineColors.Mint,
                                StreamlineColors.Aqua,
                                StreamlineColors.Amber,
                            ),
                        ),
                    ),
            )
        }
    }
}
