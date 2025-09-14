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
import com.padelscoring.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "setup") {
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
