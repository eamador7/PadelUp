package com.padelscoring.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.padelscoring.ui.theme.PadelBorderHairline
import com.padelscoring.ui.theme.PadelTextPrimary
import com.padelscoring.ui.theme.PadelVolt

/**
 * Distinctive Padel Racket Icon:
 * Aerodynamic teardrop head with signature circular ventilation drill-holes.
 */
@Composable
fun PadelRacketIcon(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    tint: Color = PadelVolt
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Racket Head (Rounded Teardrop)
            val headPath = Path().apply {
                moveTo(w * 0.5f, h * 0.05f)
                cubicTo(w * 0.85f, h * 0.05f, w * 0.9f, h * 0.45f, w * 0.65f, h * 0.68f)
                lineTo(w * 0.58f, h * 0.72f)
                lineTo(w * 0.58f, h * 0.95f) // Handle right
                lineTo(w * 0.42f, h * 0.95f) // Handle bottom
                lineTo(w * 0.42f, h * 0.72f) // Handle left
                lineTo(w * 0.35f, h * 0.68f)
                cubicTo(w * 0.1f, h * 0.45f, w * 0.15f, h * 0.05f, w * 0.5f, h * 0.05f)
                close()
            }
            drawPath(headPath, color = tint, style = Stroke(width = w * 0.08f))

            // Signature drill-holes inside the racket head
            val holeRadius = w * 0.045f
            val holeOffsets = listOf(
                Offset(w * 0.5f, h * 0.25f),
                Offset(w * 0.38f, h * 0.33f),
                Offset(w * 0.62f, h * 0.33f),
                Offset(w * 0.44f, h * 0.43f),
                Offset(w * 0.56f, h * 0.43f),
                Offset(w * 0.5f, h * 0.53f)
            )
            holeOffsets.forEach { pos ->
                drawCircle(color = tint, radius = holeRadius, center = pos)
            }
        }
    }
}

/**
 * Padel Ball Icon:
 * Vibrant sphere in Electric Volt with characteristic felt curve seam.
 */
@Composable
fun PadelBallIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = PadelVolt
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val r = this.size.minDimension / 2
            val center = Offset(r, r)

            // Outer sphere fill
            drawCircle(color = tint, radius = r, center = center)

            // Curved felt seam line
            val seamPath = Path().apply {
                moveTo(r * 0.3f, r * 0.15f)
                cubicTo(r * 0.8f, r * 0.5f, r * 0.8f, r * 1.5f, r * 0.3f, r * 1.85f)
            }
            drawPath(
                path = seamPath,
                color = Color.Black.copy(alpha = 0.4f),
                style = Stroke(width = r * 0.12f)
            )
        }
    }
}

/**
 * Padel Court Grid Icon:
 * Miniature geometric court enclosure with service boxes.
 */
@Composable
fun PadelCourtGridIcon(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    tint: Color = PadelTextPrimary
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = 1.5.dp.toPx()

            // Outer glass walls
            drawRect(
                color = tint,
                topLeft = Offset(w * 0.08f, h * 0.15f),
                size = Size(w * 0.84f, h * 0.7f),
                style = Stroke(width = strokeW)
            )
            // Net line
            drawLine(
                color = PadelVolt,
                start = Offset(w * 0.5f, h * 0.15f),
                end = Offset(w * 0.5f, h * 0.85f),
                strokeWidth = strokeW * 1.5f
            )
            // Service lines
            drawLine(
                color = tint.copy(alpha = 0.6f),
                start = Offset(w * 0.3f, h * 0.15f),
                end = Offset(w * 0.3f, h * 0.85f),
                strokeWidth = strokeW
            )
            drawLine(
                color = tint.copy(alpha = 0.6f),
                start = Offset(w * 0.7f, h * 0.15f),
                end = Offset(w * 0.7f, h * 0.85f),
                strokeWidth = strokeW
            )
        }
    }
}
