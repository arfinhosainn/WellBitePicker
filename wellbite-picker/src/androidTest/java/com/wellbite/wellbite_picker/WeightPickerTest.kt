package com.wellbite.wellbite_picker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wellbite.wellbite_picker.weight_picker.WellBiteWeightPicker
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeightPickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun weightPicker_displaysInitialWeightCorrectly() {
        composeTestRule.setContent {
            WellBiteWeightPicker(

                onWeightSelected = {}
            )
        }

        // Check the text displaying the weight
        composeTestRule
            .onNodeWithText("120.5 lbs")
            .assertIsDisplayed()
    }

    @Test
    fun weightPicker_callsOnWeightSelected_onScrollStop() {
        var selectedWeight = 0f

        composeTestRule.setContent {
            WellBiteWeightPicker(
                onWeightSelected = {
                    selectedWeight = it
                }
            )
        }

        // Scroll to index 50 which corresponds to: 80 + (50 * 0.5) = 105.0 lbs
        composeTestRule
            .onAllNodes(hasScrollAction())[0]
            .performScrollToIndex(50)

        // Wait for scroll to settle
        composeTestRule.waitForIdle()

        // Give LaunchedEffect time to update state
        composeTestRule.mainClock.advanceTimeBy(500)

        // Check that selectedWeight was updated
        assert(selectedWeight == 105.0f) {
            "Expected weight to be 105.0, but was $selectedWeight"
        }
    }
}
