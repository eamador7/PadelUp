package com.padelscoring.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.padelscoring.model.ServingTeam
import com.padelscoring.model.TeamState
import com.padelscoring.viewmodel.GameViewModel

@Composable
fun ScoringScreen(
    gameViewModel: GameViewModel,
    navController: NavController
) {
    val matchState by gameViewModel.matchState.collectAsStateWithLifecycle()
    var showNewMatchDialog by remember { mutableStateOf(false) }

    // Dialog for New Match confirmation
    if (showNewMatchDialog) {
        AlertDialog(
            onDismissRequest = { showNewMatchDialog = false },
            title = { Text("Start New Match?") },
            text = { Text("Are you sure you want to end the current match and start a new one?") },
            confirmButton = {
                Button(
                    onClick = {
                        showNewMatchDialog = false
                        navController.navigate("setup") {
                            popUpTo("setup") { inclusive = true }
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showNewMatchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog for Winner announcement
    if (matchState.isMatchOver) {
        val winnerName = if (matchState.winner == ServingTeam.TEAM_A) matchState.teamA.name else matchState.teamB.name
        AlertDialog(
            onDismissRequest = { /* Prevent dismissing by clicking outside */ },
            title = { Text("Match Over!") },
            text = { Text("$winnerName wins the match!") },
            confirmButton = {
                Button(
                    onClick = {
                        navController.navigate("setup") {
                            popUpTo("setup") { inclusive = true }
                        }
                    }
                ) {
                    Text("New Match")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(modifier = Modifier.weight(1f)) {
                val teamAName = if (matchState.server == ServingTeam.TEAM_A) "${matchState.teamA.name} 🎾" else matchState.teamA.name
                TeamScoreColumn(
                    teamName = teamAName,
                    teamState = matchState.teamA,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !matchState.isMatchOver) { gameViewModel.addPoint(ServingTeam.TEAM_A) }
                )
                val teamBName = if (matchState.server == ServingTeam.TEAM_B) "${matchState.teamB.name} 🎾" else matchState.teamB.name
                TeamScoreColumn(
                    teamName = teamBName,
                    teamState = matchState.teamB,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !matchState.isMatchOver) { gameViewModel.addPoint(ServingTeam.TEAM_B) }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { gameViewModel.undoLastPoint() },
                    enabled = !matchState.isMatchOver
                ) {
                    Text("Undo")
                }
                Button(onClick = { showNewMatchDialog = true }) {
                    Text("New Match")
                }
            }
        }
    }
}

@Composable
fun TeamScoreColumn(
    teamName: String,
    teamState: TeamState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(text = teamName, style = MaterialTheme.typography.headlineMedium)
        Text(text = "Sets: ${teamState.sets}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Games: ${teamState.games}", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = teamState.score,
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )
    }
}
