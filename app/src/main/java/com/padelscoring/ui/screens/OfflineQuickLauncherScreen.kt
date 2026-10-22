package com.padelscoring.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padelscoring.ui.components.*
import com.padelscoring.ui.theme.*
import com.padelscoring.viewmodel.OfflineLauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineQuickLauncherScreen(
    viewModel: OfflineLauncherViewModel? = null,
    onLaunchTournament: (playerNames: List<String>, targetPoints: Int) -> Unit = { _, _ -> },
    onTournamentCreated: (tournamentId: String) -> Unit = {}
) {
    val haptics = rememberPadelHaptics()
    val vmState = viewModel?.uiState?.collectAsState()?.value

    var localPlayerCount by remember { mutableIntStateOf(8) }
    var localTargetPoints by remember { mutableIntStateOf(32) }
    val playerCountOptions = listOf(4, 8, 12, 16)
    val targetPointOptions = listOf(21, 24, 32)

    val localPlayerNames = remember {
        mutableStateListOf(
            "Player 1", "Player 2", "Player 3", "Player 4",
            "Player 5", "Player 6", "Player 7", "Player 8"
        )
    }

    val selectedPlayerCount = vmState?.selectedPlayerCount ?: localPlayerCount
    val selectedTargetPoints = vmState?.selectedTargetPoints ?: localTargetPoints
    val playerRoster = vmState?.playerRoster ?: localPlayerNames

    Scaffold(
        containerColor = PadelObsidianDarkest,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "OFFLINE QUICK LAUNCHER",
                        style = PadelTypography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = PadelTextPrimary
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PadelObsidian
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PadelObsidian)
                    .padding(20.dp)
            ) {
                PadelPrimaryButton(
                    text = "KICKOFF AMERICANO ($selectedPlayerCount PLAYERS)",
                    onClick = {
                        haptics.heavyConfirm()
                        val activePlayers = playerRoster.take(selectedPlayerCount)
                        if (viewModel != null) {
                            viewModel.kickoffTournament { id ->
                                onTournamentCreated(id)
                                onLaunchTournament(activePlayers, selectedTargetPoints)
                            }
                        } else {
                            onLaunchTournament(activePlayers, selectedTargetPoints)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Privacy Guarantee Badge
            item {
                OfflineShieldBadge()
            }

            // Player Count Preset Selector
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PadelCourtCardShape)
                        .background(PadelSurface)
                        .border(1.dp, PadelBorderHairline, PadelCourtCardShape)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "TOURNAMENT ROSTER SIZE",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelTextMuted,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        playerCountOptions.forEach { count ->
                            val isSelected = selectedPlayerCount == count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("chip_players_$count")
                                    .clip(PadelShapes.medium)
                                    .background(if (isSelected) PadelVolt else PadelSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) PadelVolt else PadelBorderHairline,
                                        PadelShapes.medium
                                    )
                                    .clickable {
                                        haptics.tick()
                                        if (viewModel != null) {
                                            viewModel.setPlayerCount(count)
                                        } else {
                                            localPlayerCount = count
                                            while (localPlayerNames.size < count) {
                                                localPlayerNames.add("Player ${localPlayerNames.size + 1}")
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count",
                                    style = PadelTypography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) PadelTextInverse else PadelTextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Target Points Preset Selector
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PadelCourtCardShape)
                        .background(PadelSurface)
                        .border(1.dp, PadelBorderHairline, PadelCourtCardShape)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "TARGET POINTS PER MATCH",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelTextMuted,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        targetPointOptions.forEach { pts ->
                            val isSelected = selectedTargetPoints == pts
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(PadelShapes.medium)
                                    .background(if (isSelected) PadelVolt else PadelSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) PadelVolt else PadelBorderHairline,
                                        PadelShapes.medium
                                    )
                                    .clickable {
                                        haptics.tick()
                                        if (viewModel != null) {
                                            viewModel.setTargetPoints(pts)
                                        } else {
                                            localTargetPoints = pts
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$pts PTS",
                                    style = PadelTypography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PadelTextInverse else PadelTextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Quick Guest Name Entry
            item {
                Text(
                    text = "GUEST ROSTER ($selectedPlayerCount PLAYERS)",
                    style = PadelTypography.labelSmall.copy(
                        color = PadelTextMuted,
                        letterSpacing = 1.sp
                    )
                )
            }

            itemsIndexed(playerRoster.take(selectedPlayerCount)) { index, name ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PadelShapes.medium)
                        .background(PadelSurface)
                        .border(1.dp, PadelBorderHairline, PadelShapes.medium)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PadelSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = PadelTypography.labelSmall.copy(
                                    color = PadelVolt,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = name,
                            style = PadelTypography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = PadelTextPrimary
                            )
                        )
                    }

                    Text(
                        text = "Edit ✎",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelTextMuted
                        ),
                        modifier = Modifier.clickable {
                            // Quick rename modal or inline edit
                        }
                    )
                }
            }
        }
    }
}
