package com.padelscoring.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padelscoring.ui.theme.*

/**
 * Playtomic-inspired Dynamic Level Badge with circular confidence ring.
 * Level range: 1.00 to 7.00.
 */
@Composable
fun DynamicLevelBadge(
    level: Double,
    delta: Double? = null,
    confidenceProgress: Float = 0.85f,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background track and animated confidence ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 5.dp.toPx()
                // Outer ring track
                drawCircle(
                    color = PadelBorderHairline,
                    radius = size.minDimension / 2 - strokeWidth / 2,
                    style = Stroke(width = strokeWidth)
                )
                // Active confidence arc
                drawArc(
                    color = PadelVolt,
                    startAngle = -90f,
                    sweepAngle = 360f * confidenceProgress.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Numeric Level
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.2f", level),
                    style = PadelTypography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp,
                        color = PadelTextPrimary
                    )
                )
                Text(
                    text = "LEVEL",
                    style = PadelTypography.labelSmall.copy(
                        fontSize = 8.sp,
                        letterSpacing = 1.sp,
                        color = PadelTextMuted
                    )
                )
            }
        }

        // Progression Delta Pill
        if (delta != null) {
            val isPositive = delta >= 0
            val deltaText = if (isPositive) "+${String.format("%.2f", delta)}" else String.format("%.2f", delta)
            val pillBg = if (isPositive) PadelVoltSubtle else PadelRedSubtle
            val pillColor = if (isPositive) PadelVolt else PadelRedFault

            Box(
                modifier = Modifier
                    .clip(PadelPillShape)
                    .background(pillBg)
                    .border(1.dp, pillColor.copy(alpha = 0.4f), PadelPillShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${if (isPositive) "▲ " else "▼ "}$deltaText",
                    style = PadelTypography.labelSmall.copy(
                        color = pillColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

/**
 * Animated Rank Delta Chip for Leaderboard shifts:
 * E.g. ▲ +2, ▼ -1, ― 0
 */
@Composable
fun RankDeltaChip(
    delta: Int,
    modifier: Modifier = Modifier
) {
    val (text, color, bg) = when {
        delta > 0 -> Triple("▲ +$delta", PadelVolt, PadelVoltSubtle)
        delta < 0 -> Triple("▼ $delta", PadelRedFault, PadelRedSubtle)
        else -> Triple("―", PadelTextMuted, Color.Transparent)
    }

    AnimatedContent(
        targetState = delta,
        transitionSpec = {
            (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
        },
        label = "RankDeltaAnimation"
    ) {
        Box(
            modifier = modifier
                .clip(PadelShapes.extraSmall)
                .background(bg)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = PadelTypography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * Tactical Preferred Court Side Badge:
 * DRIVE (Right side, control & builder) vs REVÉS (Left side, power & smasher)
 */
@Composable
fun CourtSideBadge(
    side: String, // "DRIVE" or "REVÉS"
    modifier: Modifier = Modifier
) {
    val isReves = side.contains("REV", ignoreCase = true)
    val accentColor = if (isReves) PadelVolt else PadelTurfCyan

    Box(
        modifier = modifier
            .clip(PadelShapes.small)
            .background(accentColor.copy(alpha = 0.15f))
            .border(1.dp, accentColor.copy(alpha = 0.4f), PadelShapes.small)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = side.uppercase(),
            style = PadelTypography.labelSmall.copy(
                color = accentColor,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                fontSize = 10.sp
            )
        )
    }
}

/**
 * Offline Mode Guarantee Badge:
 * Highlights local-only data privacy.
 */
@Composable
fun OfflineShieldBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(PadelPillShape)
            .background(PadelSkySubtle)
            .border(1.dp, PadelSkyOffline.copy(alpha = 0.4f), PadelPillShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(PadelSkyOffline)
        )
        Text(
            text = "100% OFFLINE • LOCAL ONLY",
            style = PadelTypography.labelSmall.copy(
                color = PadelSkyOffline,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                fontSize = 10.sp
            )
        )
    }
}

/**
 * Rolling Form Dot for recent 10-match match history (W / L).
 */
@Composable
fun FormDot(
    isWin: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isWin) PadelVolt else PadelSurfaceElevated
    val textColor = if (isWin) PadelTextInverse else PadelTextMuted

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(bg)
            .border(
                1.dp,
                if (isWin) PadelVolt else PadelBorderHairline,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isWin) "W" else "L",
            style = PadelTypography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        )
    }
}
