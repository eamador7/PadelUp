package com.padelscoring.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.padelscoring.ui.screens.PlayerProfileScreen
import com.padelscoring.ui.theme.PadelTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h915dp")
class PlayerProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dynamicLevelBadgeRendersNumericRatingAndDelta() {
        composeTestRule.setContent {
            PadelTheme {
                PlayerProfileScreen(
                    level = 4.85,
                    delta = 0.12
                )
            }
        }

        // Numeric level formatted with Locale.US
        composeTestRule.onNodeWithText("4.85").assertExists()
        // Progression delta pill
        composeTestRule.onNodeWithText("▲ +0.12").assertExists()
    }

    @Test
    fun recentFormRowDisplaysCorrectCountOfWinAndLossBadges() {
        // 8 wins (true) and 2 losses (false)
        val form = listOf(true, true, true, false, true, true, true, false, true, true)

        composeTestRule.setContent {
            PadelTheme {
                PlayerProfileScreen(recentForm = form)
            }
        }

        // Verify letter counts
        composeTestRule.onAllNodesWithText("W").assertCountEquals(8)
        composeTestRule.onAllNodesWithText("L").assertCountEquals(2)

        // Verify semantic content description counts
        composeTestRule.onAllNodesWithContentDescription("Win badge").assertCountEquals(8)
        composeTestRule.onAllNodesWithContentDescription("Loss badge").assertCountEquals(2)
    }

    @Test
    fun playerStatsCardsRender() {
        composeTestRule.setContent {
            PadelTheme {
                PlayerProfileScreen()
            }
        }

        composeTestRule.onNodeWithText("Alejandro Galán").assertExists()
        composeTestRule.onNodeWithText("WIN RATE").assertExists()
        composeTestRule.onNodeWithText("BEST STREAK").assertExists()
        composeTestRule.onNodeWithText("AMERICANO TROPHIES & BADGES")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
