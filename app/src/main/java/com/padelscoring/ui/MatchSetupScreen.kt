package com.padelscoring.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.padelscoring.model.ScoringMode

import androidx.navigation.NavController
import com.padelscoring.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    gameViewModel: GameViewModel,
    navController: NavController
) {
    var teamAName by remember { mutableStateOf("") }
    var teamBName by remember { mutableStateOf("") }
    val scoringOptions = ScoringMode.values()
    var selectedScoringMode by remember { mutableStateOf(ScoringMode.BEST_OF_THREE_TRADITIONAL) }

    val isStartEnabled = teamAName.isNotBlank() && teamBName.isNotBlank()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Match Setup", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = teamAName,
                onValueChange = { teamAName = it },
                label = { Text("Team A Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = teamBName,
                onValueChange = { teamBName = it },
                label = { Text("Team B Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text("Scoring Mode", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                scoringOptions.forEach { mode ->
                    Row(
                        Modifier
                            .selectable(
                                selected = (mode == selectedScoringMode),
                                onClick = { selectedScoringMode = mode }
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (mode == selectedScoringMode),
                            onClick = { selectedScoringMode = mode }
                        )
                        Text(
                            text = if (mode == ScoringMode.BEST_OF_THREE_TRADITIONAL) "Traditional" else "Golden Point",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    gameViewModel.startNewMatch(teamAName, teamBName, selectedScoringMode)
                    navController.navigate("scoring")
                },
                enabled = isStartEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Start Match")
            }
        }
    }
}
