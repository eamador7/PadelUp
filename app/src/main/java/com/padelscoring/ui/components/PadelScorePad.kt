package com.padelscoring.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padelscoring.ui.theme.*

/**
 * Emil Kowalski Animated Digit Odometer:
 * Slides digits vertically with spring inertia when score increments or decrements.
 */
@Composable
fun AnimatedScoreDigit(
    score: Int,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = score,
        transitionSpec = {
            if (targetState > initialState) {
                // Point scored: slides up from bottom
                (slideInVertically(
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
                ) { height -> height } + fadeIn()).togetherWith(
                    slideOutVertically(
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
                    ) { height -> -height } + fadeOut()
                )
            } else {
                // Point decremented: slides down from top
                (slideInVertically(
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
                ) { height -> -height } + fadeIn()).togetherWith(
                    slideOutVertically(
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
                    ) { height -> height } + fadeOut()
                )
            }
        },
        label = "ScoreRollAnimation",
        modifier = modifier
    ) { targetScore ->
        Text(
            text = "$targetScore",
            style = PadelTypography.displayLarge.copy(
                fontSize = 54.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-2).sp,
                color = PadelTextPrimary
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Tactile Increment Button (+1, +2, -1):
 * High-speed tactile tap target with scale bounce and haptics.
 */
@Composable
fun ScoreIncrementButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    haptics: PadelHapticFeedback = rememberPadelHaptics()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "IncBtnSpring"
    )

    val bg = if (isPrimary) PadelVolt else PadelSurfaceElevated
    val textColor = if (isPrimary) PadelTextInverse else PadelTextPrimary
    val border = if (isPrimary) PadelVoltBorder else PadelBorderHairline

    Box(
        modifier = modifier
            .scale(scale)
            .height(52.dp)
            .clip(PadelShapes.medium)
            .background(bg)
            .border(1.dp, border, PadelShapes.medium)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptics.tick()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = PadelTypography.labelLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        )
    }
}

/**
 * Padel Quick Score Pad:
 * Specifically tuned for Americano matches where target total (e.g. 32 or 24) is fixed.
 * Team A + Team B = Target points.
 */
@Composable
fun PadelQuickScorePad(
    teamAName: String,
    teamBName: String,
    scoreA: Int,
    scoreB: Int,
    targetPoints: Int = 32,
    onScoreAChanged: (Int) -> Unit,
    onScoreBChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    haptics: PadelHapticFeedback = rememberPadelHaptics()
) {
    val currentTotal = scoreA + scoreB
    val isTargetReached = currentTotal == targetPoints

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(PadelCourtCardShape)
            .background(PadelSurface)
            .border(1.dp, if (isTargetReached) PadelVoltBorder else PadelBorderHairline, PadelCourtCardShape)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Target Points Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AMERICANO TOTAL",
                style = PadelTypography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    color = PadelTextMuted
                )
            )
            Box(
                modifier = Modifier
                    .clip(PadelPillShape)
                    .background(if (isTargetReached) PadelVoltSubtle else PadelSurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$currentTotal / $targetPoints PTS",
                    style = PadelTypography.labelSmall.copy(
                        color = if (isTargetReached) PadelVolt else PadelTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dual Score Counter Display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team A Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = teamAName,
                    style = PadelTypography.titleMedium.copy(
                        color = PadelTextSecondary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedScoreDigit(score = scoreA)
                Spacer(modifier = Modifier.height(12.dp))

                // Increment row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                ) {
                    ScoreIncrementButton(
                        label = "+1",
                        isPrimary = true,
                        onClick = {
                            if (scoreA + 1 <= targetPoints) {
                                onScoreAChanged(scoreA + 1)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        haptics = haptics
                    )
                    ScoreIncrementButton(
                        label = "-1",
                        isPrimary = false,
                        onClick = {
                            if (scoreA > 0) {
                                onScoreAChanged(scoreA - 1)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        haptics = haptics
                    )
                }
            }

            // Central Separator
            Text(
                text = "VS",
                style = PadelTypography.labelSmall.copy(
                    color = PadelTextMuted,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Team B Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = teamBName,
                    style = PadelTypography.titleMedium.copy(
                        color = PadelTextSecondary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedScoreDigit(score = scoreB)
                Spacer(modifier = Modifier.height(12.dp))

                // Increment row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                ) {
                    ScoreIncrementButton(
                        label = "+1",
                        isPrimary = true,
                        onClick = {
                            if (scoreB + 1 <= targetPoints) {
                                onScoreBChanged(scoreB + 1)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        haptics = haptics
                    )
                    ScoreIncrementButton(
                        label = "-1",
                        isPrimary = false,
                        onClick = {
                            if (scoreB > 0) {
                                onScoreBChanged(scoreB - 1)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        haptics = haptics
                    )
                }
            }
        }
    }
}
