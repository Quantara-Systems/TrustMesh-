package com.trustmesh.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trustmesh.designsystem.theme.TrustMeshTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * A beautiful radial gauge utilizing Compose Canvas.
 * Used for displaying Trust Scores (0-100) or Spend Envelopes.
 */
@Composable
fun TrustMeshGauge(
    progress: Float, // value between 0.0f and 1.0f
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    label: String = "",
    valueText: String = "${(progress * 100).toInt()}"
) {
    val themeColors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography
    
    // Animate the progress transition
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "gauge_progress"
    )

    // Determine the color based on status
    val gaugeColor = remember(progress) {
        when {
            progress >= 0.75f -> themeColors.primary
            progress >= 0.40f -> themeColors.secondary
            else -> themeColors.danger
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size
            val diameter = Math.min(canvasSize.width, canvasSize.height) - strokeWidth.toPx()
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(
                (canvasSize.width - diameter) / 2,
                (canvasSize.height - diameter) / 2
            )

            // Draw track (background arc of 270 degrees)
            drawArc(
                color = themeColors.divider,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Draw progress arc
            drawArc(
                color = gaugeColor,
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = valueText,
                style = typography.monetaryLarge,
                color = themeColors.textPrimary
            )
            if (label.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = typography.caption,
                    color = themeColors.textSecondary
                )
            }
        }
    }
}

/**
 * A custom Canvas-drawn radar/spider chart showing compositional trust factors.
 */
@Composable
fun TrustMeshRadarChart(
    data: Map<String, Float>, // Label to value (0f to 1f)
    modifier: Modifier = Modifier
) {
    val themeColors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography
    val textMeasurer = rememberTextMeasurer()

    val entries = remember(data) { data.toList() }
    val numAxes = entries.size

    if (numAxes < 3) return

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = (Math.min(size.width, size.height) / 2f) * 0.7f

        // 1. Draw web grid lines (levels of 25%, 50%, 75%, 100%)
        val levels = listOf(0.25f, 0.50f, 0.75f, 1.00f)
        levels.forEach { level ->
            val path = Path()
            val radius = maxRadius * level
            for (i in 0 until numAxes) {
                val angle = (2 * Math.PI * i / numAxes) - Math.PI / 2
                val x = center.x + radius * cos(angle).toFloat()
                val y = center.y + radius * sin(angle).toFloat()
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()
            drawPath(
                path = path,
                color = themeColors.divider,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 2. Draw axis lines and labels
        for (i in 0 until numAxes) {
            val angle = (2 * Math.PI * i / numAxes) - Math.PI / 2
            val edgeX = center.x + maxRadius * cos(angle).toFloat()
            val edgeY = center.y + maxRadius * sin(angle).toFloat()

            // Draw axis line from center to max edge
            drawLine(
                color = themeColors.divider,
                start = center,
                end = Offset(edgeX, edgeY),
                strokeWidth = 1.dp.toPx()
            )

            // Draw label text slightly pushed outside the edge
            val labelRadius = maxRadius + 16.dp.toPx()
            val labelX = center.x + labelRadius * cos(angle).toFloat()
            val labelY = center.y + labelRadius * sin(angle).toFloat()
            val labelText = entries[i].first

            val textLayout = textMeasurer.measure(
                text = labelText,
                style = typography.bodySmall.copy(color = themeColors.textSecondary)
            )

            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(
                    labelX - textLayout.size.width / 2f,
                    labelY - textLayout.size.height / 2f
                )
            )
        }

        // 3. Plot the data polygon
        val dataPath = Path()
        for (i in 0 until numAxes) {
            val value = entries[i].second.coerceIn(0f, 1f)
            val angle = (2 * Math.PI * i / numAxes) - Math.PI / 2
            val valRadius = maxRadius * value
            val x = center.x + valRadius * cos(angle).toFloat()
            val y = center.y + valRadius * sin(angle).toFloat()
            if (i == 0) {
                dataPath.moveTo(x, y)
            } else {
                dataPath.lineTo(x, y)
            }
        }
        dataPath.close()

        // Draw translucent filled region
        drawPath(
            path = dataPath,
            color = themeColors.primary.copy(alpha = 0.2f),
            style = Fill
        )
        // Draw solid border
        drawPath(
            path = dataPath,
            color = themeColors.primary,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * Custom modifier implementing a premium scale-down press effect.
 */
fun Modifier.pressClickable(
    onClick: () -> Unit
): Modifier = this.composed {
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    try {
                        awaitRelease()
                    } finally {
                        isPressed = false
                    }
                },
                onTap = { onClick() }
            )
        }
}

/**
 * Brush for custom skeletal loading shimmer.
 */
@Composable
fun shimmerBrush(
    showShimmer: Boolean = true,
    targetValue: Float = 1000f
): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            TrustMeshTheme.colors.surfaceElevated1,
            TrustMeshTheme.colors.surfaceElevated2,
            TrustMeshTheme.colors.surfaceElevated1
        )

        val transition = rememberInfiniteTransition(label = "shimmer_transition")
        val translateAnimation by transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translation"
        )

        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation, y = translateAnimation)
        )
    } else {
        SolidColor(Color.Transparent)
    }
}

/**
 * Reusable modifier to apply a beautiful shimmer animation.
 */
fun Modifier.shimmer(
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(8.dp)
): Modifier = this.composed {
    if (enabled) {
        val brush = shimmerBrush()
        this.clip(shape).background(brush)
    } else {
        this
    }
}

/**
 * A premium custom Canvas-drawn Area Chart with gradient fill showing historical transaction trends.
 */
@Composable
fun TrustMeshAreaChart(
    points: List<Float>,
    modifier: Modifier = Modifier
) {
    val themeColors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas

        val marginStart = 40.dp.toPx()
        val marginEnd = 16.dp.toPx()
        val marginTop = 16.dp.toPx()
        val marginBottom = 24.dp.toPx()

        val chartWidth = size.width - marginStart - marginEnd
        val chartHeight = size.height - marginTop - marginBottom

        val maxVal = points.maxOrNull() ?: 100f
        val minVal = 0f
        val range = maxVal - minVal

        // 1. Draw horizontal grid lines (3 levels)
        val numGridLines = 3
        for (i in 0..numGridLines) {
            val ratio = i.toFloat() / numGridLines
            val y = marginTop + chartHeight * (1f - ratio)
            drawLine(
                color = themeColors.divider,
                start = Offset(marginStart, y),
                end = Offset(size.width - marginEnd, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // 2. Build paths for line and fill
        val linePath = Path()
        val fillPath = Path()

        val stepX = chartWidth / (points.size - 1).coerceAtLeast(1)

        points.forEachIndexed { index, value ->
            val ratioY = if (range != 0f) (value - minVal) / range else 0.5f
            val x = marginStart + index * stepX
            val y = marginTop + chartHeight * (1f - ratioY)

            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, marginTop + chartHeight)
                fillPath.lineTo(x, y)
            } else {
                val prevRatioY = if (range != 0f) (points[index - 1] - minVal) / range else 0.5f
                val prevX = marginStart + (index - 1) * stepX
                val prevY = marginTop + chartHeight * (1f - prevRatioY)

                val controlX1 = prevX + stepX / 2f
                val controlY1 = prevY
                val controlX2 = prevX + stepX / 2f
                val controlY2 = y

                linePath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }

            if (index == points.size - 1) {
                fillPath.lineTo(x, marginTop + chartHeight)
                fillPath.close()
            }
        }

        // 3. Draw gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    themeColors.primary.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                startY = marginTop,
                endY = marginTop + chartHeight
            )
        )

        // 4. Draw smooth trend line
        drawPath(
            path = linePath,
            color = themeColors.primary,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 5. Draw data points as neat dots
        points.forEachIndexed { index, value ->
            val ratioY = if (range != 0f) (value - minVal) / range else 0.5f
            val x = marginStart + index * stepX
            val y = marginTop + chartHeight * (1f - ratioY)

            drawCircle(
                color = themeColors.backgroundBase,
                radius = 5.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = themeColors.primary,
                radius = 3.dp.toPx(),
                center = Offset(x, y),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

