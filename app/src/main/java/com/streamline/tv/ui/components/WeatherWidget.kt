package com.streamline.tv.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamline.tv.data.TemperatureUnit
import com.streamline.tv.data.weather.WeatherCondition
import com.streamline.tv.data.weather.WeatherSnapshot
import com.streamline.tv.ui.theme.StreamlineColors

@Composable
fun WeatherWidget(
    weather: WeatherSnapshot?,
    unit: TemperatureUnit,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .glassSurface(shape = RoundedCornerShape(22.dp), alpha = 0.09f, borderAlpha = 0.16f)
            .padding(start = 18.dp, end = 22.dp, top = 12.dp, bottom = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (weather == null) {
            WeatherGlyph(
                condition = if (loading) WeatherCondition.PARTLY_CLOUDY else WeatherCondition.UNKNOWN,
                isDay = true,
                modifier = Modifier.size(38.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column {
                BasicText(
                    text = if (loading) "Checking weather…" else "Weather unavailable",
                    style = TextStyle(color = StreamlineColors.Cloud, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                )
                BasicText(
                    text = "Set a location in Settings",
                    style = TextStyle(color = StreamlineColors.Mist, fontSize = 12.sp),
                )
            }
            return@Row
        }

        WeatherGlyph(
            condition = weather.condition,
            isDay = weather.isDay,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column {
            Row(verticalAlignment = Alignment.Top) {
                BasicText(
                    text = "${weather.temperature}°",
                    style = TextStyle(
                        color = StreamlineColors.Cloud,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Medium,
                        fontFeatureSettings = "tnum",
                    ),
                )
                BasicText(
                    text = if (unit == TemperatureUnit.FAHRENHEIT) "F" else "C",
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp),
                    style = TextStyle(color = StreamlineColors.Mist, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                )
            }
            BasicText(
                text = "${weather.condition.label}  ·  H ${weather.high}°  L ${weather.low}°",
                style = TextStyle(color = StreamlineColors.Mist, fontSize = 13.sp),
            )
            BasicText(
                text = weather.locationLabel,
                style = TextStyle(color = StreamlineColors.Mist.copy(alpha = 0.75f), fontSize = 12.sp),
            )
        }
    }
}

@Composable
fun WeatherGlyph(
    condition: WeatherCondition,
    isDay: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val unit = size.minDimension
        val center = Offset(size.width / 2f, size.height / 2f)
        val cloudColor = Color(0xFFE9F2FA)
        val sunColor = StreamlineColors.Amber
        val rainColor = StreamlineColors.Aqua

        fun sun(at: Offset, radius: Float) {
            rotate(degrees = 0f, pivot = at) {
                repeat(8) { index ->
                    rotate(degrees = index * 45f, pivot = at) {
                        drawLine(
                            color = sunColor.copy(alpha = 0.9f),
                            start = Offset(at.x, at.y - radius * 1.55f),
                            end = Offset(at.x, at.y - radius * 2.05f),
                            strokeWidth = radius * 0.18f,
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }
            drawCircle(sunColor, radius, at)
        }

        fun cloud(at: Offset, width: Float, color: Color) {
            val height = width * 0.46f
            drawCircle(color, height * 0.52f, Offset(at.x - width * 0.22f, at.y))
            drawCircle(color, height * 0.68f, Offset(at.x, at.y - height * 0.18f))
            drawCircle(color, height * 0.48f, Offset(at.x + width * 0.24f, at.y + height * 0.04f))
            drawRoundRect(
                color = color,
                topLeft = Offset(at.x - width * 0.40f, at.y - height * 0.02f),
                size = Size(width * 0.80f, height * 0.55f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(height * 0.27f),
            )
        }

        fun rain(at: Offset, width: Float, drops: Int = 3) {
            repeat(drops) { index ->
                val x = at.x - width * 0.26f + index * width * 0.26f
                drawLine(
                    color = rainColor,
                    start = Offset(x, at.y + width * 0.10f),
                    end = Offset(x - width * 0.05f, at.y + width * 0.30f),
                    strokeWidth = width * 0.055f,
                    cap = StrokeCap.Round,
                )
            }
        }

        fun snow(at: Offset, width: Float) {
            repeat(3) { index ->
                val x = at.x - width * 0.26f + index * width * 0.26f
                drawCircle(Color.White, width * 0.045f, Offset(x, at.y + width * 0.22f))
            }
        }

        when (condition) {
            WeatherCondition.CLEAR -> {
                if (isDay) {
                    sun(center, unit * 0.16f)
                } else {
                    drawCircle(Color(0xFFEAF2FB), unit * 0.20f, center)
                    drawCircle(StreamlineColors.Ink.copy(alpha = 0.9f), unit * 0.18f, center + Offset(unit * 0.09f, -unit * 0.07f))
                }
            }

            WeatherCondition.PARTLY_CLOUDY -> {
                if (isDay) sun(center - Offset(unit * 0.14f, unit * 0.14f), unit * 0.13f)
                else drawCircle(Color(0xFFEAF2FB), unit * 0.13f, center - Offset(unit * 0.14f, unit * 0.12f))
                cloud(center + Offset(unit * 0.04f, unit * 0.10f), unit * 0.78f, cloudColor)
            }

            WeatherCondition.CLOUDY -> {
                cloud(center - Offset(unit * 0.10f, unit * 0.12f), unit * 0.66f, cloudColor.copy(alpha = 0.75f))
                cloud(center + Offset(unit * 0.08f, unit * 0.08f), unit * 0.72f, cloudColor)
            }

            WeatherCondition.FOG -> {
                cloud(center - Offset(0f, unit * 0.14f), unit * 0.72f, cloudColor.copy(alpha = 0.8f))
                repeat(3) { index ->
                    val y = center.y + unit * (0.12f + index * 0.09f)
                    drawLine(
                        color = cloudColor.copy(alpha = 0.55f - index * 0.10f),
                        start = Offset(center.x - unit * 0.34f, y),
                        end = Offset(center.x + unit * 0.34f, y),
                        strokeWidth = unit * 0.045f,
                        cap = StrokeCap.Round,
                    )
                }
            }

            WeatherCondition.DRIZZLE, WeatherCondition.RAIN -> {
                cloud(center - Offset(0f, unit * 0.10f), unit * 0.74f, cloudColor)
                rain(center + Offset(0f, unit * 0.06f), unit * 0.62f, if (condition == WeatherCondition.RAIN) 3 else 2)
            }

            WeatherCondition.SNOW -> {
                cloud(center - Offset(0f, unit * 0.10f), unit * 0.74f, cloudColor)
                snow(center + Offset(0f, unit * 0.02f), unit * 0.60f)
            }

            WeatherCondition.THUNDERSTORM -> {
                cloud(center - Offset(0f, unit * 0.12f), unit * 0.74f, cloudColor)
                val bolt = Path().apply {
                    moveTo(center.x + unit * 0.04f, center.y + unit * 0.02f)
                    lineTo(center.x - unit * 0.10f, center.y + unit * 0.22f)
                    lineTo(center.x + unit * 0.01f, center.y + unit * 0.22f)
                    lineTo(center.x - unit * 0.06f, center.y + unit * 0.40f)
                    lineTo(center.x + unit * 0.14f, center.y + unit * 0.16f)
                    lineTo(center.x + unit * 0.03f, center.y + unit * 0.16f)
                    close()
                }
                drawPath(bolt, StreamlineColors.Amber)
            }

            WeatherCondition.UNKNOWN -> {
                drawCircle(cloudColor.copy(alpha = 0.8f), unit * 0.30f, center, style = Stroke(width = unit * 0.05f))
                drawCircle(StreamlineColors.Mist, unit * 0.05f, center + Offset(0f, unit * 0.20f))
            }
        }
    }
}

private fun DrawScope.drawRainDrop(start: Offset, length: Float, color: Color) {
    drawLine(color, start, start + Offset(-length * 0.15f, length), strokeWidth = length * 0.16f, cap = StrokeCap.Round)
}
