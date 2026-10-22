package com.padelscoring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.padelscoring.data.local.PadelDatabase
import com.padelscoring.ui.MatchSetupScreen
import com.padelscoring.ui.ScoringScreen
import com.padelscoring.ui.screens.AmericanoCommandCenterScreen
import com.padelscoring.ui.screens.GroupsHubScreen
import com.padelscoring.ui.screens.MatchConfirmationScreen
import com.padelscoring.ui.screens.OfflineQuickLauncherScreen
import com.padelscoring.ui.screens.PlayerProfileScreen
import com.padelscoring.ui.theme.PadelTheme
import com.padelscoring.viewmodel.AmericanoTournamentViewModel
import com.padelscoring.viewmodel.GameViewModel
import com.padelscoring.viewmodel.GroupsHubViewModel
import com.padelscoring.viewmodel.MatchConfirmationViewModel
import com.padelscoring.viewmodel.OfflineLauncherViewModel
import com.padelscoring.viewmodel.PadelViewModelFactory
import com.padelscoring.viewmodel.PlayerProfileViewModel

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PadelTheme {
                val context = LocalContext.current
                val database = remember { PadelDatabase.getInstance(context) }
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "americano") {
                    composable(
                        route = "americano?id={id}",
                        arguments = listOf(
                            navArgument("id") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        val tournamentId = backStackEntry.arguments?.getString("id")
                        val factory = remember(tournamentId) {
                            PadelViewModelFactory.fromDatabase(database, initialId = tournamentId)
                        }
                        val vm: AmericanoTournamentViewModel = viewModel(factory = factory)
                        AmericanoCommandCenterScreen(
                            viewModel = vm,
                            onNavigateBack = { navController.popBackStack() },
                            onMatchSelected = { navController.navigate("confirmation") }
                        )
                    }

                    composable(
                        route = "confirmation?matchId={matchId}",
                        arguments = listOf(
                            navArgument("matchId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        val matchId = backStackEntry.arguments?.getString("matchId")
                        val factory = remember(matchId) {
                            PadelViewModelFactory.fromDatabase(database, initialId = matchId)
                        }
                        val vm: MatchConfirmationViewModel = viewModel(factory = factory)
                        MatchConfirmationScreen(
                            viewModel = vm,
                            onConfirmed = { navController.navigate("americano") },
                            onDisputeSubmitted = { navController.navigate("americano") }
                        )
                    }

                    composable("profile") {
                        val factory = remember { PadelViewModelFactory.fromDatabase(database) }
                        val vm: PlayerProfileViewModel = viewModel(factory = factory)
                        PlayerProfileScreen(viewModel = vm)
                    }

                    composable("groups") {
                        val factory = remember { PadelViewModelFactory.fromDatabase(database) }
                        val vm: GroupsHubViewModel = viewModel(factory = factory)
                        GroupsHubScreen(
                            viewModel = vm,
                            onStartAmericanoOnline = { navController.navigate("americano") },
                            onStartAmericanoOffline = { navController.navigate("quick_launcher") }
                        )
                    }

                    composable("quick_launcher") {
                        val factory = remember { PadelViewModelFactory.fromDatabase(database) }
                        val vm: OfflineLauncherViewModel = viewModel(factory = factory)
                        OfflineQuickLauncherScreen(
                            viewModel = vm,
                            onLaunchTournament = { _, _ -> },
                            onTournamentCreated = { id ->
                                navController.navigate("americano?id=$id")
                            }
                        )
                    }

                    composable("setup") {
                        MatchSetupScreen(
                            gameViewModel = gameViewModel,
                            navController = navController
                        )
                    }

                    composable("scoring") {
                        ScoringScreen(
                            gameViewModel = gameViewModel,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
