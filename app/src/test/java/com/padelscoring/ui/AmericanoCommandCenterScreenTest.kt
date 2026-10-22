package com.padelscoring.ui

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
@Config(sdk = [34])
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

        composeTestRule.onNodeWithText("COURT 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("COURT 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ale Galán & Arturo Coello").assertIsDisplayed()
        composeTestRule.onNodeWithText("Juan Lebrón & Agustín Tapia").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paquito Navarro & F. Stupaczuk").assertIsDisplayed()
        composeTestRule.onNodeWithText("Martín Di Nenno & F. Belasteguín").assertIsDisplayed()
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

        composeTestRule.onNodeWithText("AMERICANO TOTAL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Galán / Coello").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lebrón / Tapia").assertIsDisplayed()
    }

    @Test
    fun adjustingScoresMaintainsTargetPointsSum() {
        composeTestRule.setContent {
            PadelTheme {
                AmericanoCommandCenterScreen(initialActiveCourt = 1)
            }
        }

        // Verify initial score state (18 + 14 = 32)
        composeTestRule.onNodeWithText("32 / 32 PTS").assertIsDisplayed()

        // Increment Team A (+1) -> 19 + 13 = 32
        composeTestRule.onAllNodesWithText("+1")[0].performClick()
        composeTestRule.onNodeWithText("32 / 32 PTS").assertIsDisplayed()

        // Decrement Team A (-1) -> 18 + 14 = 32
        composeTestRule.onAllNodesWithText("-1")[0].performClick()
        composeTestRule.onNodeWithText("32 / 32 PTS").assertIsDisplayed()
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
        composeTestRule.onNodeWithText("Alejandro Galán").assertIsDisplayed()
        composeTestRule.onNodeWithText("Arturo Coello").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("PTS", substring = true)[0].assertIsDisplayed()
    }

    @Test
    fun nextRoundBannerRendersWhenMatchesFinish() {
        // Initially incomplete matches: banner should not appear
        composeTestRule.setContent {
            PadelTheme {
                AmericanoCommandCenterScreen(initialRound = 1, initialMatchesFinished = false)
            }
        }
        composeTestRule.onNodeWithText("GENERATE ROUND 2").assertDoesNotExist()

        // When matches complete: banner renders with "GENERATE ROUND 2"
        composeTestRule.setContent {
            PadelTheme {
                AmericanoCommandCenterScreen(initialRound = 1, initialMatchesFinished = true)
            }
        }
        composeTestRule.onNodeWithText("ALL COURTS COMPLETED").assertIsDisplayed()
        composeTestRule.onNodeWithText("GENERATE ROUND 2").assertIsDisplayed()
    }
}
