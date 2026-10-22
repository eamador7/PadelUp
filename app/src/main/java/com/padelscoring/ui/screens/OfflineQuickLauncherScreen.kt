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
            "Player 5", "Player 6", "Player 7", "Player 8",
            "Player 9", "Player 10", "Player 11", "Player 12",
            "Player 13", "Player 14", "Player 15", "Player 16"
        )
    }

    var editingPlayerIndex by remember { mutableStateOf<Int?>(null) }
    var editingPlayerName by remember { mutableStateOf("") }

    val selectedPlayerCount = vmState?.selectedPlayerCount ?: localPlayerCount
    val selectedTargetPoints = vmState?.selectedTargetPoints ?: localTargetPoints
    val playerRoster = vmState?.playerRoster ?: localPlayerNames

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
                            PadelRacketIcon(size = 18.dp)
                            Text(
                                text = "OFFLINE QUICK LAUNCHER",
                                style = PadelTypography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    color = PadelVolt
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Instant Tournament Setup",
                            style = PadelTypography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PadelTextPrimary
                            )
                        )
                    }

                    // Estimated Duration Badge
                    val estimatedMinutes = when (selectedPlayerCount) {
                        4 -> 35
                        8 -> 70
                        12 -> 105
                        else -> 140
                    }
                    Box(
                        modifier = Modifier
                            .clip(PadelPillShape)
                            .background(PadelSurfaceElevated)
                            .border(1.dp, PadelBorderHairline, PadelPillShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "⏱ ~$estimatedMinutes MIN",
                            style = PadelTypography.labelSmall.copy(
                                color = PadelVolt,
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOURNAMENT ROSTER SIZE",
                            style = PadelTypography.labelSmall.copy(
                                color = PadelTextMuted,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "${selectedPlayerCount / 4} ${if (selectedPlayerCount == 4) "COURT" else "COURTS"}",
                            style = PadelTypography.labelSmall.copy(
                                color = PadelTurfCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        playerCountOptions.forEach { count ->
                            val isSelected = selectedPlayerCount == count
                            Column(
                                modifier = Modifier
                                    .weight(1f)
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
                                    }
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$count",
                                    style = PadelTypography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) PadelTextInverse else PadelTextPrimary
                                    )
                                )
                                Text(
                                    text = "PLAYERS",
                                    style = PadelTypography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PadelTextInverse.copy(alpha = 0.8f) else PadelTextMuted
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TARGET POINTS PER MATCH",
                            style = PadelTypography.labelSmall.copy(
                                color = PadelTextMuted,
                                letterSpacing = 1.sp
                            )
                        )
                        val matchFormatName = when (selectedTargetPoints) {
                            21 -> "SPRINT"
                            24 -> "STANDARD"
                            else -> "PRO MASTERS"
                        }
                        Text(
                            text = matchFormatName,
                            style = PadelTypography.labelSmall.copy(
                                color = PadelVolt,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

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

            // Primary Launch Action
            item {
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

            // Quick Guest Name Entry
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GUEST ROSTER ($selectedPlayerCount PLAYERS)",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelTextMuted,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "TAP TO RENAME",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelVolt,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            itemsIndexed(playerRoster.take(selectedPlayerCount)) { index, name ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PadelShapes.medium)
                        .background(PadelSurface)
                        .border(1.dp, PadelBorderHairline, PadelShapes.medium)
                        .clickable {
                            editingPlayerIndex = index
                            editingPlayerName = name
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PadelSurfaceElevated)
                                .border(1.dp, PadelBorderHairline, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = PadelTypography.labelSmall.copy(
                                    color = PadelVolt,
                                    fontWeight = FontWeight.ExtraBold
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

                    Box(
                        modifier = Modifier
                            .clip(PadelPillShape)
                            .background(PadelSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Edit ✎",
                            style = PadelTypography.labelSmall.copy(
                                color = PadelTextMuted,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }

    // Modal dialogue to rename a guest player
    if (editingPlayerIndex != null) {
        val idx = editingPlayerIndex!!
        AlertDialog(
            onDismissRequest = { editingPlayerIndex = null },
            title = {
                Text(
                    text = "Player ${idx + 1} Name",
                    style = PadelTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PadelTextPrimary
                    )
                )
            },
            text = {
                OutlinedTextField(
                    value = editingPlayerName,
                    onValueChange = { editingPlayerName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PadelTextPrimary,
                        unfocusedTextColor = PadelTextPrimary,
                        focusedBorderColor = PadelVolt,
                        unfocusedBorderColor = PadelBorderHairline,
                        cursorColor = PadelVolt
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = editingPlayerName.trim()
                        if (trimmed.isNotEmpty()) {
                            if (viewModel != null) {
                                viewModel.updatePlayerName(idx, trimmed)
                            } else if (idx < localPlayerNames.size) {
                                localPlayerNames[idx] = trimmed
                            }
                        }
                        editingPlayerIndex = null
                    }
                ) {
                    Text("SAVE", color = PadelVolt, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPlayerIndex = null }) {
                    Text("CANCEL", color = PadelTextMuted)
                }
            },
            containerColor = PadelSurfaceElevated
        )
    }
}
}
