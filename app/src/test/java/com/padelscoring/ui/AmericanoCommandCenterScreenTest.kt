package com.padelscoring.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.padelscoring.ui.screens.AmericanoCommandCenterScreen
import com.padelscoring.ui.theme.PadelTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h915dp")
class AmericanoCommandCenterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersCourt1AndCourt2VisualizersWithPlayerNames() {
        composeTestRule.setContent {
            PadelTheme {
                AmericanoCommandCenterScreen()
            }
        }

        composeTestRule.onNodeWithText("COURT 1").assertExists()
        composeTestRule.onNodeWithText("COURT 2").assertExists()
        composeTestRule.onNodeWithText("Ale Galán & Arturo Coello").assertExists()
        composeTestRule.onNodeWithText("Juan Lebrón & Agustín Tapia").assertExists()
        composeTestRule.onNodeWithText("Paquito Navarro & F. Stupaczuk").assertExists()
        composeTestRule.onNodeWithText("Martín Di Nenno & F. Belasteguín").assertExists()
    }

    @Test
    fun clickingCourtOpensScorePad() {
        composeTestRule.setContent {
            PadelTheme {
                AmericanoCommandCenterScreen(initialActiveCourt = null)
            }
        }

        // Score pad should initially be closed
        composeTestRule.onNodeWithText("AMERICANO TOTAL").assertDoesNotExist()

        // Clicking Court 1 opens its score pad
        composeTestRule.onNodeWithText("COURT 1").performClick()

        composeTestRule.onNodeWithText("AMERICANO TOTAL").assertExists()
        composeTestRule.onNodeWithText("Galán / Coello").assertExists()
        composeTestRule.onNodeWithText("Lebrón / Tapia").assertExists()
    }

    @Test
    fun adjustingScoresMaintainsTargetPointsSum() {
        composeTestRule.setContent {
            PadelTheme {
                AmericanoCommandCenterScreen(initialActiveCourt = 1)
            }
        }

        // Verify initial score state (18 + 14 = 32)
        composeTestRule.onNodeWithText("32 / 32 PTS").assertExists()

        // Increment Team A (+1) -> 19 + 13 = 32
        composeTestRule.onAllNodesWithText("+1")[0].performClick()
        composeTestRule.onNodeWithText("32 / 32 PTS").assertExists()

        // Decrement Team A (-1) -> 18 + 14 = 32
        composeTestRule.onAllNodesWithText("-1")[0].performClick()
        composeTestRule.onNodeWithText("32 / 32 PTS").assertExists()
    }

    @Test
    fun liveLeaderboardReflectsScoreChanges() {
        composeTestRule.setContent {
            PadelTheme {
                AmericanoCommandCenterScreen(initialActiveCourt = 1)
            }
        }

        // Adjust score on Court 1
        composeTestRule.onAllNodesWithText("+1")[0].performClick()

        // Switch to Live Standings tab
        composeTestRule.onNodeWithText("LIVE STANDINGS").performClick()

        // Verify leaderboard is rendered
        composeTestRule.onNodeWithText("Alejandro Galán").assertExists()
        composeTestRule.onNodeWithText("Arturo Coello").assertExists()
        composeTestRule.onAllNodesWithText("PTS", substring = true)[0].assertExists()
    }

    @Test
    fun nextRoundBannerRendersWhenMatchesFinish() {
        var matchesFinished by mutableStateOf(false)

        composeTestRule.setContent {
            PadelTheme {
                AmericanoCommandCenterScreen(
                    initialRound = 1,
                    initialMatchesFinished = matchesFinished
                )
            }
        }

        // Initially incomplete matches: banner should not appear
        composeTestRule.onNodeWithText("GENERATE ROUND 2").assertDoesNotExist()

        // Complete matches: banner renders with "GENERATE ROUND 2"
        matchesFinished = true
        composeTestRule.onNodeWithText("ALL COURTS COMPLETED").assertExists()
        composeTestRule.onNodeWithText("GENERATE ROUND 2").assertExists()
    }
}
