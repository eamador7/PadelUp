package com.padelscoring.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padelscoring.ui.components.*
import com.padelscoring.ui.theme.*
import com.padelscoring.viewmodel.AmericanoTournamentViewModel

// Data model for Americano player standing
data class AmericanoPlayerStanding(
    val id: String,
    val name: String,
    val rank: Int,
    val rankDelta: Int,
    val played: Int,
    val won: Int,
    val pointDiff: Int,
    val totalPoints: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmericanoCommandCenterScreen(
    viewModel: AmericanoTournamentViewModel? = null,
    onNavigateBack: () -> Unit = {},
    onMatchSelected: (courtNumber: Int) -> Unit = {}
) {
    val haptics = rememberPadelHaptics()
    val vmState = viewModel?.uiState?.collectAsState()?.value

    var mockCurrentRound by remember { mutableIntStateOf(3) }
    val mockTotalRounds = 7
    val mockTargetPoints = 32

    // Interactive fallback court state for standalone preview
    var court1ScoreA by remember { mutableIntStateOf(18) }
    var court1ScoreB by remember { mutableIntStateOf(14) }
    var court1Finished by remember { mutableStateOf(false) }

    var court2ScoreA by remember { mutableIntStateOf(16) }
    var court2ScoreB by remember { mutableIntStateOf(16) }
    var court2Finished by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Active Courts, 1 = Live Standings

    val mockStandings = remember {
        mutableStateListOf(
            AmericanoPlayerStanding("1", "Alejandro Galán", 1, 2, 2, 2, +12, 42),
            AmericanoPlayerStanding("2", "Arturo Coello", 2, 0, 2, 2, +8, 38),
            AmericanoPlayerStanding("3", "Juan Lebrón", 3, -1, 2, 1, +4, 34),
            AmericanoPlayerStanding("4", "Agustín Tapia", 4, 1, 2, 1, +2, 32),
            AmericanoPlayerStanding("5", "Paquito Navarro", 5, -2, 2, 1, -2, 28),
            AmericanoPlayerStanding("6", "Franco Stupaczuk", 6, 0, 2, 1, -4, 26),
            AmericanoPlayerStanding("7", "Martín Di Nenno", 7, 1, 2, 0, -8, 22),
            AmericanoPlayerStanding("8", "Fernando Belasteguín", 8, -1, 2, 0, -12, 18)
        )
    }

    val tournamentTitle = vmState?.tournamentName ?: "Friday Night Masters"
    val currentRound = vmState?.currentRoundNumber ?: mockCurrentRound
    val totalRounds = vmState?.totalRounds ?: mockTotalRounds
    val targetPoints = vmState?.targetPoints ?: mockTargetPoints
    val standings = vmState?.standings?.takeIf { it.isNotEmpty() } ?: mockStandings
    val courtMatches = vmState?.courtMatches ?: emptyList()

    Scaffold(
        containerColor = PadelObsidianDarkest,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PadelObsidian)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
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
                            PadelRacketIcon(size = 20.dp)
                            Text(
                                text = "AMERICANO COMMAND",
                                style = PadelTypography.labelSmall.copy(
                                    color = PadelVolt,
                                    letterSpacing = 1.2.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tournamentTitle,
                            style = PadelTypography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PadelTextPrimary
                            )
                        )
                    }

                    // Round Pill Badge
                    Box(
                        modifier = Modifier
                            .clip(PadelPillShape)
                            .background(PadelVoltSubtle)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ROUND $currentRound OF $totalRounds",
                            style = PadelTypography.labelSmall.copy(
                                color = PadelVolt,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (currentRound.toFloat() / totalRounds.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(PadelPillShape),
                    color = PadelVolt,
                    trackColor = PadelBorderHairline
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector: Courts vs Leaderboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .clip(PadelShapes.medium)
                    .background(PadelSurface)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PadelShapes.small)
                        .background(if (selectedTab == 0) PadelVolt else PadelSurface)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ACTIVE COURTS",
                        style = PadelTypography.labelLarge.copy(
                            color = if (selectedTab == 0) PadelTextInverse else PadelTextSecondary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PadelShapes.small)
                        .background(if (selectedTab == 1) PadelVolt else PadelSurface)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LIVE STANDINGS",
                        style = PadelTypography.labelLarge.copy(
                            color = if (selectedTab == 1) PadelTextInverse else PadelTextSecondary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            if (selectedTab == 0) {
                // Active Courts View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (courtMatches.isNotEmpty()) {
                        courtMatches.forEach { match ->
                            item(key = match.matchId) {
                                PadelCourtVisualizerCard(
                                    courtNumber = match.courtNumber,
                                    teamAPlayers = match.teamANames,
                                    teamBPlayers = match.teamBNames,
                                    currentScore = "${match.teamAScore} - ${match.teamBScore}",
                                    isCompleted = match.isCompleted,
                                    onClick = { onMatchSelected(match.courtNumber) }
                                )
                            }

                            item(key = "${match.matchId}_score") {
                                PadelQuickScorePad(
                                    teamAName = "${match.teamANames.first} / ${match.teamANames.second}",
                                    teamBName = "${match.teamBNames.first} / ${match.teamBNames.second}",
                                    scoreA = match.teamAScore,
                                    scoreB = match.teamBScore,
                                    targetPoints = targetPoints,
                                    onScoreAChanged = { newA ->
                                        val newB = (targetPoints - newA).coerceAtLeast(0)
                                        viewModel?.onScoreChanged(match.matchId, newA, newB)
                                    },
                                    onScoreBChanged = { newB ->
                                        val newA = (targetPoints - newB).coerceAtLeast(0)
                                        viewModel?.onScoreChanged(match.matchId, newA, newB)
                                    },
                                    haptics = haptics
                                )
                            }
                        }
                    } else {
                        // Fallback preview courts
                        item {
                            PadelCourtVisualizerCard(
                                courtNumber = 1,
                                teamAPlayers = Pair("Ale Galán", "Arturo Coello"),
                                teamBPlayers = Pair("Juan Lebrón", "Agustín Tapia"),
                                currentScore = "$court1ScoreA - $court1ScoreB",
                                isCompleted = court1Finished,
                                onClick = { onMatchSelected(1) }
                            )
                        }

                        item {
                            PadelQuickScorePad(
                                teamAName = "Galán / Coello",
                                teamBName = "Lebrón / Tapia",
                                scoreA = court1ScoreA,
                                scoreB = court1ScoreB,
                                targetPoints = targetPoints,
                                onScoreAChanged = { court1ScoreA = it },
                                onScoreBChanged = { court1ScoreB = it },
                                haptics = haptics
                            )
                        }

                        item {
                            PadelCourtVisualizerCard(
                                courtNumber = 2,
                                teamAPlayers = Pair("Paquito Navarro", "F. Stupaczuk"),
                                teamBPlayers = Pair("Martín Di Nenno", "F. Belasteguín"),
                                currentScore = "$court2ScoreA - $court2ScoreB",
                                isCompleted = court2Finished,
                                onClick = { onMatchSelected(2) }
                            )
                        }
                    }

                    // Next Round Generation Trigger Banner
                    item {
                        PadelNextRoundBanner(
                            nextRoundNumber = currentRound + 1,
                            onGenerateNextRound = {
                                haptics.heavyConfirm()
                                if (viewModel != null) {
                                    viewModel.onGenerateNextRound()
                                } else if (mockCurrentRound < mockTotalRounds) {
                                    mockCurrentRound++
                                    court1ScoreA = 0
                                    court1ScoreB = 0
                                    court2ScoreA = 0
                                    court2ScoreB = 0
                                }
                            }
                        )
                    }
                }
            } else {
                // Live Standings View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(standings, key = { _, item -> item.id }) { _, standing ->
                        PadelLeaderboardRow(
                            rank = standing.rank,
                            playerName = standing.name,
                            played = standing.played,
                            won = standing.won,
                            pointDiff = standing.pointDiff,
                            totalPoints = standing.totalPoints,
                            rankDelta = standing.rankDelta
                        )
                    }
                }
            }
        }
    }
}
