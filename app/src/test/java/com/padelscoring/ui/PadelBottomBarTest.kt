package com.padelscoring.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.padelscoring.ui.navigation.PadelBottomBar
import com.padelscoring.ui.navigation.PadelNavigationItem
import com.padelscoring.ui.theme.PadelTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h915dp")
class PadelBottomBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomBarRendersCoreTabs() {
        composeTestRule.setContent {
            PadelTheme {
                PadelBottomBar(
                    currentRoute = "americano",
                    onNavigateToDestination = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Americano").assertIsDisplayed()
        composeTestRule.onNodeWithText("Groups").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_americano").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_groups").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_profile").assertIsDisplayed()
    }

    @Test
    fun tabSelectionHighlightsActiveDestination() {
        composeTestRule.setContent {
            PadelTheme {
                PadelBottomBar(
                    currentRoute = "americano",
                    onNavigateToDestination = {}
                )
            }
        }

        // Americano should be selected, others unselected
        composeTestRule.onNodeWithTag("nav_americano").assertIsSelected()
        composeTestRule.onNodeWithTag("nav_groups").assertIsNotSelected()
        composeTestRule.onNodeWithTag("nav_profile").assertIsNotSelected()
    }

    @Test
    fun tappingTabNavigatesToDestination() {
        var selectedItem: PadelNavigationItem? = null

        composeTestRule.setContent {
            PadelTheme {
                PadelBottomBar(
                    currentRoute = "americano",
                    onNavigateToDestination = { item -> selectedItem = item }
                )
            }
        }

        composeTestRule.onNodeWithTag("nav_profile").performClick()
        assertEquals(PadelNavigationItem.Profile, selectedItem)

        composeTestRule.onNodeWithTag("nav_groups").performClick()
        assertEquals(PadelNavigationItem.Groups, selectedItem)

        composeTestRule.onNodeWithTag("nav_americano").performClick()
        assertEquals(PadelNavigationItem.Americano, selectedItem)
    }
}
