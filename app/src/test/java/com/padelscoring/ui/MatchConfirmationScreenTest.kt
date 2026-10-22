package com.padelscoring.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.padelscoring.ui.screens.MatchConfirmationScreen
import com.padelscoring.ui.theme.PadelTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h915dp")
class MatchConfirmationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun slideToConfirmActivatesCallbackWhenDraggedPastThreshold() {
        var confirmed = false
        composeTestRule.setContent {
            PadelTheme {
                MatchConfirmationScreen(
                    onConfirmed = { confirmed = true }
                )
            }
        }

        assertFalse(confirmed)

        // Drag the thumb past threshold
        composeTestRule.onNodeWithText("▶").performTouchInput {
            down(center)
            moveBy(Offset(1000f, 0f))
            up()
        }

        assertTrue(confirmed)
        composeTestRule.onNodeWithText("CONFIRMED ✓").assertExists()
    }

    @Test
    fun slideToConfirmDoesNotActivateOnPartialDrag() {
        var confirmed = false
        composeTestRule.setContent {
            PadelTheme {
                MatchConfirmationScreen(
                    onConfirmed = { confirmed = true }
                )
            }
        }

        // Partial drag (< 85% threshold)
        composeTestRule.onNodeWithText("▶").performTouchInput {
            down(center)
            moveBy(Offset(20f, 0f))
            up()
        }

        assertFalse(confirmed)
        composeTestRule.onNodeWithText("CONFIRMED ✓").assertDoesNotExist()
    }

    @Test
    fun clickingDisputeScoreDisplaysReasonDialog() {
        composeTestRule.setContent {
            PadelTheme {
                MatchConfirmationScreen()
            }
        }

        // Reason sheet should not be visible initially
        composeTestRule.onNodeWithText("SELECT DISPUTE REASON").assertDoesNotExist()

        // Tap Dispute Score link
        composeTestRule.onNodeWithText("Dispute Score", substring = true)
            .performScrollTo()
            .performClick()

        // Verify dispute options and submit button appear
        composeTestRule.onNodeWithText("SELECT DISPUTE REASON").assertExists()
        composeTestRule.onNodeWithText("Incorrect score recorded").assertExists()
        composeTestRule.onNodeWithText("Wrong players in pairing").assertExists()
        composeTestRule.onNodeWithText("Match prematurely ended / injury").assertExists()
        composeTestRule.onNodeWithText("Rule interpretation conflict").assertExists()
        composeTestRule.onNodeWithText("SUBMIT DISPUTE TO COORDINATOR").assertExists()
    }
}
