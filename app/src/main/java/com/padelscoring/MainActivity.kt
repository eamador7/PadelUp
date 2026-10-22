package com.padelscoring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.padelscoring.ui.MatchSetupScreen
import com.padelscoring.ui.ScoringScreen
import com.padelscoring.ui.screens.AmericanoCommandCenterScreen
import com.padelscoring.ui.screens.GroupsHubScreen
import com.padelscoring.ui.screens.MatchConfirmationScreen
import com.padelscoring.ui.screens.OfflineQuickLauncherScreen
import com.padelscoring.ui.screens.PlayerProfileScreen
import com.padelscoring.ui.theme.PadelTheme
import com.padelscoring.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PadelTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "americano") {
                    composable("americano") {
                        AmericanoCommandCenterScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onMatchSelected = { navController.navigate("confirmation") }
                        )
                    }
                    composable("confirmation") {
                        MatchConfirmationScreen(
                            onConfirmed = { navController.navigate("americano") },
                            onDisputeSubmitted = { navController.navigate("americano") }
                        )
                    }
                    composable("profile") {
                        PlayerProfileScreen()
                    }
                    composable("groups") {
                        GroupsHubScreen(
                            onStartAmericanoOnline = { navController.navigate("americano") },
                            onStartAmericanoOffline = { navController.navigate("quick_launcher") }
                        )
                    }
                    composable("quick_launcher") {
                        OfflineQuickLauncherScreen(
                            onLaunchTournament = { _, _ -> navController.navigate("americano") }
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

