package com.padelscoring.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.padelscoring.model.ScoringMode
import com.padelscoring.ui.components.*
import com.padelscoring.ui.theme.*
import com.padelscoring.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    gameViewModel: GameViewModel,
    navController: NavController
) {
    val haptics = rememberPadelHaptics()

    var player1 by remember { mutableStateOf("Player 1") }
    var player2 by remember { mutableStateOf("Player 2") }
    var player3 by remember { mutableStateOf("Player 3") }
    var player4 by remember { mutableStateOf("Player 4") }

    var selectedScoringMode by remember { mutableStateOf(ScoringMode.BEST_OF_THREE_GOLDEN_POINT) }

    Scaffold(
        containerColor = PadelObsidianDarkest,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PadelObsidian)
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PadelRacketIcon(size = 18.dp, tint = PadelBrandBlue)
                            Text(
                                text = "SINGLE 2V2 MATCH SETUP",
                                style = PadelTypography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    color = PadelBrandBlue
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Regulation Padel",
                            style = PadelTypography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PadelTextPrimary
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(PadelPillShape)
                            .background(PadelSurfaceElevated)
                            .border(1.dp, PadelBorderHairline, PadelPillShape)
                            .clickable { navController.popBackStack() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "CANCEL",
                            style = PadelTypography.labelSmall.copy(
                                color = PadelTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Team A Card
            item {
                TeamRosterInputCard(
                    teamBadge = "TEAM A",
                    badgeColor = PadelBrandBlue,
                    badgeBg = PadelBrandBlueSubtle,
                    playerOneName = player1,
                    onPlayerOneChange = { player1 = it },
                    playerTwoName = player2,
                    onPlayerTwoChange = { player2 = it }
                )
            }

            // VS Divider
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PadelSurfaceElevated)
                            .border(1.dp, PadelBorderHairline, CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "VS",
                            style = PadelTypography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PadelTurfCyan,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            // Team B Card
            item {
                TeamRosterInputCard(
                    teamBadge = "TEAM B",
                    badgeColor = PadelAmberTiebreak,
                    badgeBg = PadelAmberSubtle,
                    playerOneName = player3,
                    onPlayerOneChange = { player3 = it },
                    playerTwoName = player4,
                    onPlayerTwoChange = { player4 = it }
                )
            }

            // Scoring Mode Selector (Golden Point vs Traditional Advantage)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PadelCourtCardShape)
                        .background(PadelSurface)
                        .border(1.dp, PadelBorderHairline, PadelCourtCardShape)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DEUCE SCORING RULE",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelTextMuted,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Golden Point Card
                        val isGolden = selectedScoringMode == ScoringMode.BEST_OF_THREE_GOLDEN_POINT
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(PadelShapes.medium)
                                .background(if (isGolden) PadelBrandBlueSubtle else PadelSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isGolden) PadelBrandBlue else PadelBorderHairline,
                                    PadelShapes.medium
                                )
                                .clickable {
                                    haptics.tick()
                                    selectedScoringMode = ScoringMode.BEST_OF_THREE_GOLDEN_POINT
                                }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("⚡", fontSize = 14.sp)
                                    Text(
                                        text = "PUNTO DE ORO",
                                        style = PadelTypography.labelSmall.copy(
                                            color = if (isGolden) PadelBrandBlue else PadelTextPrimary,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sudden death at 40-40. Receiving team chooses side.",
                                    style = PadelTypography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = PadelTextSecondary,
                                        lineHeight = 14.sp
                                    )
                                )
                            }
                        }

                        // Traditional Advantage Card
                        val isTraditional = selectedScoringMode == ScoringMode.BEST_OF_THREE_TRADITIONAL
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(PadelShapes.medium)
                                .background(if (isTraditional) PadelBrandBlueSubtle else PadelSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isTraditional) PadelBrandBlue else PadelBorderHairline,
                                    PadelShapes.medium
                                )
                                .clickable {
                                    haptics.tick()
                                    selectedScoringMode = ScoringMode.BEST_OF_THREE_TRADITIONAL
                                }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("⚖️", fontSize = 14.sp)
                                    Text(
                                        text = "VENTAJA",
                                        style = PadelTypography.labelSmall.copy(
                                            color = if (isTraditional) PadelBrandBlue else PadelTextPrimary,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Traditional Advantage. Team must win by 2 clear points.",
                                    style = PadelTypography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = PadelTextSecondary,
                                        lineHeight = 14.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Start Match CTA
            item {
                Spacer(modifier = Modifier.height(8.dp))
                PadelPrimaryButton(
                    text = "🎾 START 2V2 MATCH",
                    onClick = {
                        val teamA = "${player1.trim().ifEmpty { "Player 1" }} / ${player2.trim().ifEmpty { "Player 2" }}"
                        val teamB = "${player3.trim().ifEmpty { "Player 3" }} / ${player4.trim().ifEmpty { "Player 4" }}"
                        gameViewModel.startNewMatch(teamA, teamB, selectedScoringMode)
                        navController.navigate("scoring")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TeamRosterInputCard(
    teamBadge: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    badgeBg: androidx.compose.ui.graphics.Color,
    playerOneName: String,
    onPlayerOneChange: (String) -> Unit,
    playerTwoName: String,
    onPlayerTwoChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PadelCourtCardShape)
            .background(PadelSurface)
            .border(1.dp, PadelBorderHairline, PadelCourtCardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(PadelPillShape)
                    .background(badgeBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = teamBadge,
                    style = PadelTypography.labelSmall.copy(
                        color = badgeColor,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
            }
            Text(
                text = "DRIVE & REVÉS",
                style = PadelTypography.labelSmall.copy(
                    color = PadelTextMuted,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp
                )
            )
        }

        OutlinedTextField(
            value = playerOneName,
            onValueChange = onPlayerOneChange,
            label = { Text("Player 1 Name") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PadelSurfaceElevated,
                unfocusedContainerColor = PadelSurfaceElevated,
                focusedBorderColor = PadelBrandBlue,
                unfocusedBorderColor = PadelBorderHairline,
                focusedTextColor = PadelTextPrimary,
                unfocusedTextColor = PadelTextPrimary,
                focusedLabelColor = PadelBrandBlue,
                unfocusedLabelColor = PadelTextMuted
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = PadelShapes.medium
        )

        OutlinedTextField(
            value = playerTwoName,
            onValueChange = onPlayerTwoChange,
            label = { Text("Player 2 Name") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PadelSurfaceElevated,
                unfocusedContainerColor = PadelSurfaceElevated,
                focusedBorderColor = PadelBrandBlue,
                unfocusedBorderColor = PadelBorderHairline,
                focusedTextColor = PadelTextPrimary,
                unfocusedTextColor = PadelTextPrimary,
                focusedLabelColor = PadelBrandBlue,
                unfocusedLabelColor = PadelTextMuted
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = PadelShapes.medium
        )
    }
}
