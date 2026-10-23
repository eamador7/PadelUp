package com.padelscoring.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.padelscoring.model.ScoringMode
import com.padelscoring.model.ServingTeam
import com.padelscoring.model.TeamState
import com.padelscoring.ui.components.*
import com.padelscoring.ui.theme.*
import com.padelscoring.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringScreen(
    gameViewModel: GameViewModel,
    navController: NavController
) {
    val haptics = rememberPadelHaptics()
    val matchState by gameViewModel.matchState.collectAsStateWithLifecycle()
    var showNewMatchDialog by remember { mutableStateOf(false) }

    val isGoldenPointDeuce = !matchState.isTieBreakActive &&
            matchState.teamA.score == "40" && matchState.teamB.score == "40" &&
            matchState.scoringMode == ScoringMode.BEST_OF_THREE_GOLDEN_POINT

    // Dialog for New Match confirmation
    if (showNewMatchDialog) {
        AlertDialog(
            onDismissRequest = { showNewMatchDialog = false },
            containerColor = PadelSurfaceElevated,
            title = {
                Text(
                    text = "End Current Match?",
                    style = PadelTypography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PadelTextPrimary
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to discard the current live scorecard and start a new match?",
                    style = PadelTypography.bodyMedium.copy(color = PadelTextSecondary)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptics.heavyConfirm()
                        showNewMatchDialog = false
                        navController.navigate("setup") {
                            popUpTo("setup") { inclusive = true }
                        }
                    }
                ) {
                    Text("START OVER", color = PadelRedFault, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewMatchDialog = false }) {
                    Text("RESUME", color = PadelTextPrimary)
                }
            }
        )
    }

    // Dialog for Match Winner announcement
    if (matchState.isMatchOver) {
        val winnerName = if (matchState.winner == ServingTeam.TEAM_A) matchState.teamA.name else matchState.teamB.name
        AlertDialog(
            onDismissRequest = { /* Prevent dismissing by tapping outside */ },
            containerColor = PadelSurfaceElevated,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "🏆", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "MATCH COMPLETED!",
                        style = PadelTypography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = PadelTextPrimary
                        )
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = winnerName,
                        style = PadelTypography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = PadelBrandBlue,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "wins the match in standard sets.",
                        style = PadelTypography.bodyMedium.copy(
                            color = PadelTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .clip(PadelPillShape)
                            .background(PadelSurface)
                            .border(1.dp, PadelBorderHairline, PadelPillShape)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${matchState.teamA.name}: ${matchState.teamA.sets} SETS",
                            style = PadelTypography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (matchState.winner == ServingTeam.TEAM_A) PadelBrandBlue else PadelTextMuted
                            )
                        )
                        Text(
                            text = "${matchState.teamB.name}: ${matchState.teamB.sets} SETS",
                            style = PadelTypography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (matchState.winner == ServingTeam.TEAM_B) PadelBrandBlue else PadelTextMuted
                            )
                        )
                    }
                }
            },
            confirmButton = {
                PadelPrimaryButton(
                    text = "🎾 PLAY NEW MATCH",
                    onClick = {
                        haptics.heavyConfirm()
                        navController.navigate("setup") {
                            popUpTo("setup") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        haptics.tick()
                        navController.navigate("americano") {
                            popUpTo("americano") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "EXIT TO HOME",
                        style = PadelTypography.labelMedium.copy(
                            color = PadelTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        )
    }

    Scaffold(
        containerColor = PadelObsidianDarkest,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PadelObsidian)
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
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
                                .background(PadelBrandBlue)
                        )
                        Text(
                            text = "LIVE 2V2 MATCH",
                            style = PadelTypography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PadelBrandBlue,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(PadelPillShape)
                                .background(PadelSurfaceElevated)
                                .border(1.dp, PadelBorderHairline, PadelPillShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (matchState.scoringMode == ScoringMode.BEST_OF_THREE_GOLDEN_POINT) "PUNTO DE ORO" else "VENTAJA",
                                style = PadelTypography.labelSmall.copy(
                                    color = PadelTurfCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(PadelPillShape)
                                .background(PadelSurfaceElevated)
                                .border(1.dp, PadelBorderHairline, PadelPillShape)
                                .clickable { showNewMatchDialog = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "RESET",
                                style = PadelTypography.labelSmall.copy(
                                    color = PadelRedFault,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High-voltage alert banners
            AnimatedVisibility(
                visible = matchState.isTieBreakActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PadelAmberTiebreak.copy(alpha = 0.2f))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎾 TIE-BREAK ACTIVE • FIRST TO 7 PTS (WIN BY 2)",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelAmberTiebreak,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = isGoldenPointDeuce,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PadelBrandBlue.copy(alpha = 0.25f))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ PUNTO DE ORO • NEXT POINT WINS THE GAME",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelTurfCyan,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                    )
                }
            }

            // Compact Sets & Games Overview Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PadelSurface)
                    .border(1.dp, PadelBorderHairline)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = matchState.teamA.name,
                        style = PadelTypography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PadelTextPrimary
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "SETS: ${matchState.teamA.sets}  •  GAMES: ${matchState.teamA.games}",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelBrandBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PadelSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "VS",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelTextMuted,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = matchState.teamB.name,
                        style = PadelTypography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PadelTextPrimary
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "GAMES: ${matchState.teamB.games}  •  SETS: ${matchState.teamB.sets}",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelAmberTiebreak,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Interactive Split-Court Thumb Scoring Zones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Team A Zone
                InteractiveTeamCourtZone(
                    modifier = Modifier.weight(1f),
                    teamName = matchState.teamA.name,
                    teamState = matchState.teamA,
                    isServing = matchState.server == ServingTeam.TEAM_A,
                    accentColor = PadelBrandBlue,
                    accentSubtle = PadelBrandBlueSubtle,
                    isMatchOver = matchState.isMatchOver,
                    onClick = {
                        haptics.heavyConfirm()
                        gameViewModel.addPoint(ServingTeam.TEAM_A)
                    }
                )

                // Team B Zone
                InteractiveTeamCourtZone(
                    modifier = Modifier.weight(1f),
                    teamName = matchState.teamB.name,
                    teamState = matchState.teamB,
                    isServing = matchState.server == ServingTeam.TEAM_B,
                    accentColor = PadelAmberTiebreak,
                    accentSubtle = PadelAmberSubtle,
                    isMatchOver = matchState.isMatchOver,
                    onClick = {
                        haptics.heavyConfirm()
                        gameViewModel.addPoint(ServingTeam.TEAM_B)
                    }
                )
            }

            // Bottom Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Undo Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(PadelShapes.medium)
                        .background(PadelSurfaceElevated)
                        .border(1.dp, PadelBorderHairline, PadelShapes.medium)
                        .clickable(enabled = !matchState.isMatchOver) {
                            haptics.tick()
                            gameViewModel.undoLastPoint()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("↩", fontSize = 16.sp, color = PadelTextPrimary)
                        Text(
                            text = "UNDO POINT",
                            style = PadelTypography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PadelTextPrimary
                            )
                        )
                    }
                }

                // New Match Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(PadelShapes.medium)
                        .background(PadelSurfaceElevated)
                        .border(1.dp, PadelBorderHairline, PadelShapes.medium)
                        .clickable {
                            haptics.tick()
                            showNewMatchDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NEW MATCH",
                        style = PadelTypography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PadelTextSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveTeamCourtZone(
    modifier: Modifier = Modifier,
    teamName: String,
    teamState: TeamState,
    isServing: Boolean,
    accentColor: Color,
    accentSubtle: Color,
    isMatchOver: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isMatchOver) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "CourtZoneScale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .clip(PadelCourtCardShape)
            .background(PadelSurface)
            .border(
                width = if (isServing) 2.dp else 1.dp,
                color = if (isServing) accentColor else PadelBorderHairline,
                shape = PadelCourtCardShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isMatchOver,
                onClick = onClick
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Server Tag & Team Name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isServing) {
                    Box(
                        modifier = Modifier
                            .clip(PadelPillShape)
                            .background(accentSubtle)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🎾 SERVING",
                            style = PadelTypography.labelSmall.copy(
                                color = accentColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Text(
                    text = teamName,
                    style = PadelTypography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PadelTextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2
                )
            }

            // Center: Massive Point Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = teamState.score,
                    style = PadelTypography.displayLarge.copy(
                        fontSize = 68.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        textAlign = TextAlign.Center
                    )
                )
                Text(
                    text = "POINTS",
                    style = PadelTypography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = PadelTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Bottom: Tap to Add Point Hint
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PadelPillShape)
                    .background(PadelSurfaceElevated)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TAP TO SCORE +1",
                    style = PadelTypography.labelSmall.copy(
                        color = PadelTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
            }
        }
    }
}
