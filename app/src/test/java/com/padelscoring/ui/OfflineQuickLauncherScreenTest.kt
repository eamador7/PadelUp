package com.padelscoring.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.padelscoring.ui.screens.OfflineQuickLauncherScreen
import com.padelscoring.ui.theme.PadelTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h915dp")
class OfflineQuickLauncherScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tappingPlayerCountChipsUpdatesRosterCountDynamically() {
        composeTestRule.setContent {
            PadelTheme {
                OfflineQuickLauncherScreen()
            }
        }

        // Default roster is 8 players
        composeTestRule.onNodeWithText("GUEST ROSTER (8 PLAYERS)").assertExists()

        // Tap 4-player chip
        composeTestRule.onNodeWithTag("chip_players_4").performClick()
        composeTestRule.onNodeWithText("GUEST ROSTER (4 PLAYERS)").assertExists()

        // Tap 12-player chip
        composeTestRule.onNodeWithTag("chip_players_12").performClick()
        composeTestRule.onNodeWithText("GUEST ROSTER (12 PLAYERS)").assertExists()

        // Tap 16-player chip
        composeTestRule.onNodeWithTag("chip_players_16").performClick()
        composeTestRule.onNodeWithText("GUEST ROSTER (16 PLAYERS)").assertExists()
    }

    @Test
    fun tappingTargetPointChipsUpdatesMatchLength() {
        var launchedTarget = 0
        composeTestRule.setContent {
            PadelTheme {
                OfflineQuickLauncherScreen(
                    onLaunchTournament = { _, points -> launchedTarget = points }
                )
            }
        }

        // Tap 21 PTS preset
        composeTestRule.onNodeWithText("21 PTS").performClick()
        composeTestRule.onNodeWithText("KICKOFF AMERICANO", substring = true).performClick()
        assertEquals(21, launchedTarget)

        // Tap 24 PTS preset
        composeTestRule.onNodeWithText("24 PTS").performClick()
        composeTestRule.onNodeWithText("KICKOFF AMERICANO", substring = true).performClick()
        assertEquals(24, launchedTarget)

        // Tap 32 PTS preset
        composeTestRule.onNodeWithText("32 PTS").performClick()
        composeTestRule.onNodeWithText("KICKOFF AMERICANO", substring = true).performClick()
        assertEquals(32, launchedTarget)
    }

    @Test
    fun tappingKickoffAmericanoTriggersLaunch() {
        var launchedPlayers: List<String>? = null
        var launchedPoints: Int? = null

        composeTestRule.setContent {
            PadelTheme {
                OfflineQuickLauncherScreen(
                    onLaunchTournament = { players, points ->
                        launchedPlayers = players
                        launchedPoints = points
                    }
                )
            }
        }

        // Select 4 players
        composeTestRule.onNodeWithTag("chip_players_4").performClick()

        // Select 24 PTS
        composeTestRule.onNodeWithText("24 PTS").performClick()

        // Click launch button
        composeTestRule.onNodeWithText("KICKOFF AMERICANO", substring = true).performClick()

        assertNotNull(launchedPlayers)
        assertEquals(4, launchedPlayers?.size)
        assertEquals(24, launchedPoints)
    }
}
