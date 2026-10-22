package com.padelscoring.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padelscoring.ui.theme.*

/**
 * Padel Court Visualizer Card:
 * Renders an athletic 2D top-down padel court visualizer (blue turf, white service lines, glass perimeter)
 * with active pairs on Court 1, Court 2, etc.
 */
@Composable
fun PadelCourtVisualizerCard(
    courtNumber: Int,
    teamAPlayers: Pair<String, String>,
    teamBPlayers: Pair<String, String>,
    currentScore: String = "0 - 0",
    isCompleted: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Pulse animation for live active court
    val infiniteTransition = rememberInfiniteTransition(label = "CourtPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(PadelCourtCardShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        if (isCompleted) PadelSurface else PadelSurfaceElevated,
                        PadelSurface
                    )
                )
            )
            .border(
                1.dp,
                if (isCompleted) PadelVoltBorder else PadelBorderHairline,
                PadelCourtCardShape
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        // Court Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) PadelVolt else PadelTurfCyan.copy(alpha = pulseAlpha)
                        )
                )
                Text(
                    text = "COURT $courtNumber",
                    style = PadelTypography.labelLarge.copy(
                        color = PadelTextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Box(
                modifier = Modifier
                    .clip(PadelPillShape)
                    .background(if (isCompleted) PadelVoltSubtle else PadelSurfaceElevated)
                    .border(
                        1.dp,
                        if (isCompleted) PadelVolt.copy(alpha = 0.3f) else PadelBorderHairline,
                        PadelPillShape
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isCompleted) "FINAL ✓" else "LIVE MATCH",
                    style = PadelTypography.labelSmall.copy(
                        color = if (isCompleted) PadelVolt else PadelTurfCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mini Schematic Court Turf Illustration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .clip(PadelShapes.medium)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0F2642), Color(0xFF0A1B30))
                    )
                )
                .border(1.dp, PadelBorderHairline, PadelShapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 1.5.dp.toPx()
                val lineCol = Color.White.copy(alpha = 0.45f)

                // Court perimeter boundary
                drawRect(
                    color = lineCol,
                    topLeft = Offset(12.dp.toPx(), 8.dp.toPx()),
                    size = Size(size.width - 24.dp.toPx(), size.height - 16.dp.toPx()),
                    style = Stroke(width = strokeW)
                )
                // Center Net Line
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = Offset(size.width / 2, 8.dp.toPx()),
                    end = Offset(size.width / 2, size.height - 8.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
                // Service line Left
                drawLine(
                    color = lineCol,
                    start = Offset(size.width * 0.28f, 8.dp.toPx()),
                    end = Offset(size.width * 0.28f, size.height - 8.dp.toPx()),
                    strokeWidth = strokeW
                )
                // Service line Right
                drawLine(
                    color = lineCol,
                    start = Offset(size.width * 0.72f, 8.dp.toPx()),
                    end = Offset(size.width * 0.72f, size.height - 8.dp.toPx()),
                    strokeWidth = strokeW
                )
                // Center Service Line
                drawLine(
                    color = lineCol,
                    start = Offset(size.width * 0.28f, size.height / 2),
                    end = Offset(size.width * 0.72f, size.height / 2),
                    strokeWidth = strokeW
                )
            }

            // Overlay Current Match Score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentScore,
                    style = PadelTypography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PadelTextPrimary,
                        fontSize = 30.sp,
                        letterSpacing = (-1).sp
                    )
                )
                Text(
                    text = if (isCompleted) "TAP TO REVIEW" else "TAP TO SCORE",
                    style = PadelTypography.labelSmall.copy(
                        color = if (isCompleted) PadelVolt else PadelTextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Players Row: Team A vs Team B
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team A
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${teamAPlayers.first} & ${teamAPlayers.second}",
                    style = PadelTypography.titleSmall.copy(
                        color = PadelTextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "TEAM A",
                    style = PadelTypography.labelSmall.copy(
                        color = PadelVolt,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                )
            }

            Text(
                text = "VS",
                style = PadelTypography.labelSmall.copy(
                    color = PadelTextMuted,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Team B
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${teamBPlayers.first} & ${teamBPlayers.second}",
                    style = PadelTypography.titleSmall.copy(
                        color = PadelTextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "TEAM B",
                    style = PadelTypography.labelSmall.copy(
                        color = PadelTurfCyan,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

/**
 * Padel Leaderboard Standing Row:
 * Real-time row showing rank, avatar initials, player name, delta chip, and points.
 */
@Composable
fun PadelLeaderboardRow(
    rank: Int,
    playerName: String,
    played: Int,
    won: Int,
    pointDiff: Int,
    totalPoints: Int,
    rankDelta: Int = 0,
    modifier: Modifier = Modifier
) {
    val isPodium = rank <= 3
    val rankColor = when (rank) {
        1 -> PadelGoldCrown
        2 -> PadelSilverMedal
        3 -> PadelBronzeMedal
        else -> PadelTextSecondary
    }

    val cardBg = when (rank) {
        1 -> PadelVoltSubtle
        2 -> PadelSilverSubtle
        3 -> PadelBronzeSubtle
        else -> PadelSurface
    }

    val cardBorder = when (rank) {
        1 -> PadelVoltBorder
        2 -> PadelSilverMedal.copy(alpha = 0.3f)
        3 -> PadelBronzeMedal.copy(alpha = 0.3f)
        else -> PadelBorderHairline
    }

    val initials = playerName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(PadelShapes.medium)
            .background(cardBg)
            .border(1.dp, cardBorder, PadelShapes.medium)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Number / Crown for #1
        Row(
            modifier = Modifier.width(36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "#$rank",
                style = PadelTypography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = rankColor,
                    fontSize = 16.sp
                )
            )
        }

        // Rank Delta Chip
        RankDeltaChip(delta = rankDelta)

        Spacer(modifier = Modifier.width(10.dp))

        // Avatar Initials Circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (rank == 1) PadelVolt else PadelSurfaceElevated)
                .border(
                    1.dp,
                    if (rank == 1) PadelVolt else PadelBorderHairline,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = PadelTypography.labelMedium.copy(
                    color = if (rank == 1) PadelTextInverse else PadelTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Player Name & stats
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playerName,
                style = PadelTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PadelTextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$played P • $won W",
                style = PadelTypography.bodySmall.copy(
                    color = PadelTextMuted,
                    fontSize = 11.sp
                )
            )
        }

        // Point Difference
        val diffPrefix = if (pointDiff > 0) "+$pointDiff" else "$pointDiff"
        Text(
            text = diffPrefix,
            style = PadelTypography.labelMedium.copy(
                color = if (pointDiff >= 0) PadelVolt else PadelRedFault,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        // Total Points (Bold Hero Metric)
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$totalPoints",
                style = PadelTypography.headlineMedium.copy(
                    color = if (isPodium) PadelVolt else PadelTextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            )
            Text(
                text = "PTS",
                style = PadelTypography.labelSmall.copy(
                    color = PadelTextMuted,
                    fontSize = 9.sp
                )
            )
        }
    }
}

/**
 * Tournament Podium Card (Peak-End emotional celebration):
 * Displays celebratory Top 3 athletes with gold crown, silver and bronze pedestals.
 */
@Composable
fun TournamentPodiumCard(
    firstPlayer: Pair<String, Int>,
    secondPlayer: Pair<String, Int>,
    thirdPlayer: Pair<String, Int>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(PadelCourtCardShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E2838), PadelSurface)
                )
            )
            .border(1.5.dp, PadelGoldCrown.copy(alpha = 0.6f), PadelCourtCardShape)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "👑", fontSize = 22.sp)
            Text(
                text = "AMERICANO CHAMPIONS PODIUM",
                style = PadelTypography.labelSmall.copy(
                    color = PadelGoldCrown,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3-Pedestal Olympic/Tournament Podium Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // #2 Silver (Left)
            PodiumPillar(
                rank = 2,
                name = secondPlayer.first,
                points = secondPlayer.second,
                color = PadelSilverMedal,
                height = 100.dp,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // #1 Gold (Center, Elevated)
            PodiumPillar(
                rank = 1,
                name = firstPlayer.first,
                points = firstPlayer.second,
                color = PadelGoldCrown,
                height = 126.dp,
                isChampion = true,
                modifier = Modifier.weight(1.1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // #3 Bronze (Right)
            PodiumPillar(
                rank = 3,
                name = thirdPlayer.first,
                points = thirdPlayer.second,
                color = PadelBronzeMedal,
                height = 84.dp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PodiumPillar(
    rank: Int,
    name: String,
    points: Int,
    color: Color,
    height: androidx.compose.ui.unit.Dp,
    isChampion: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isChampion) {
            Text(text = "👑", fontSize = 16.sp)
        }
        Text(
            text = name.split(" ").firstOrNull() ?: name,
            style = PadelTypography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PadelTextPrimary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "$points PTS",
            style = PadelTypography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color.copy(alpha = if (isChampion) 0.25f else 0.15f))
                .border(
                    1.dp,
                    color.copy(alpha = 0.5f),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                style = PadelTypography.headlineMedium.copy(
                    color = color,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            )
        }
    }
}

/**
 * Tactical "Next Round Generation" Banner:
 * Appears when all courts are finished to advance the Americano tournament.
 */
@Composable
fun PadelNextRoundBanner(
    nextRoundNumber: Int,
    onGenerateNextRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(PadelCourtCardShape)
            .background(PadelSurfaceElevated)
            .border(1.5.dp, PadelVolt, PadelCourtCardShape)
            .padding(18.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PadelVolt)
                )
                Text(
                    text = "ALL COURTS COMPLETED",
                    style = PadelTypography.labelLarge.copy(
                        color = PadelVolt,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Round results are locked. Ready to shuffle pairings.",
                style = PadelTypography.bodyMedium.copy(
                    color = PadelTextSecondary,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            PadelPrimaryButton(
                text = "GENERATE ROUND $nextRoundNumber",
                onClick = onGenerateNextRound,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
