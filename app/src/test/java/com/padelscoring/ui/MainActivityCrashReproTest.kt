package com.padelscoring.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.padelscoring.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityCrashReproTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun tapOnMenusDoesNotCrash() {
        composeTestRule.waitForIdle()

        // Tap Profile
        composeTestRule.onNodeWithTag("nav_profile").performClick()
        composeTestRule.waitForIdle()

        // Tap Groups
        composeTestRule.onNodeWithTag("nav_groups").performClick()
        composeTestRule.waitForIdle()

        // Tap Quick Launch
        composeTestRule.onNodeWithTag("nav_quick_launcher").performClick()
        composeTestRule.waitForIdle()

        // Tap Americano
        composeTestRule.onNodeWithTag("nav_americano").performClick()
        composeTestRule.waitForIdle()
    }
}
