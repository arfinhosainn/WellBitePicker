package com.wellbite.wellbite_picker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wellbite.wellbite_picker.wellbite_agepicker.WellBiteAgePicker
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalFoundationApi::class)
class WellBiteAgePickerTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testInitialSelectedItem() {
        // Given
        val items = listOf("18", "19", "20", "21", "22")
        val selectedItem = "20"

        // When
        composeRule.setContent {
            WellBiteAgePicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = {},
                modifier = Modifier.testTag("agePicker")
            )
        }

        // Then
        // The picker should display the initially selected item in the middle position
        composeRule.onNodeWithText(selectedItem).assertIsDisplayed()
        // Check that surrounding items are also visible
        composeRule.onNodeWithText("19").assertIsDisplayed()
        composeRule.onNodeWithText("21").assertIsDisplayed()
    }

    @Test
    fun testItemSelection() {
        // Given
        val items = listOf("18", "19", "20", "21", "22")
        var currentSelection = "20"

        // When
        composeRule.setContent {
            var selectedItem by remember { mutableStateOf(currentSelection) }

            WellBiteAgePicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it },
                modifier = Modifier.testTag("agePicker")
            )
        }

        // Then
        // Verify initial selection
        composeRule.onNodeWithText(currentSelection).assertIsDisplayed()

        // Simulate scroll to select a different item
        composeRule.onNodeWithTag("agePicker").performTouchInput {
            swipeUp()
        }

        // Wait for animation to complete
        composeRule.waitForIdle()

        // Verify that a different item is now selected (specific item depends on scroll behavior)
        // This assertion might need adjustment based on exact scroll behavior
        composeRule.onNodeWithText("21").assertIsDisplayed()
    }

    @Test
    fun testCustomVisibleItemsCount() {
        // Given
        val items = listOf("18", "19", "20", "21", "22", "23", "24")
        val selectedItem = "21"
        val visibleItemsCount = 3 // Custom visible items count

        // When
        composeRule.setContent {
            WellBiteAgePicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = {},
                visibleItemsCount = visibleItemsCount,
                modifier = Modifier.testTag("agePicker")
            )
        }

        // Then
        // The middle item should be displayed
        composeRule.onNodeWithText(selectedItem).assertIsDisplayed()

        // With visibleItemsCount = 3, we should see fewer items than default
        // Items too far from center should not be visible
        composeRule.onNodeWithText("24").assertDoesNotExist()
        composeRule.onNodeWithText("18").assertDoesNotExist()
    }

    @Test
    fun testCustomTextStyle() {
        // Given
        val items = listOf("18", "19", "20")
        val selectedItem = "19"
        val customTextStyle = TextStyle(fontSize = 24.sp, color = Color.Red)

        // When
        composeRule.setContent {
            WellBiteAgePicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = {},
                textStyle = customTextStyle,
                modifier = Modifier.testTag("agePicker")
            )
        }

        // Then
        // Visual assertions are difficult in unit tests, but we can at least verify
        // that the component renders with our custom style without crashing
        composeRule.onNodeWithTag("agePicker").assertExists()
        composeRule.onNodeWithText(selectedItem).assertExists()
    }

    @Test
    fun testCustomDividerColor() {
        // Given
        val items = listOf("18", "19", "20")
        val selectedItem = "19"
        val dividerColor = Color.Blue

        // When
        composeRule.setContent {
            WellBiteAgePicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = {},
                dividerColor = dividerColor,
                modifier = Modifier.testTag("agePicker")
            )
        }

        // Then
        // Visual assertions for color are difficult in unit tests
        // We're testing that it renders without crashing with custom color
        composeRule.onNodeWithTag("agePicker").assertExists()
    }

    @Test
    fun testEmptyItemsList() {
        // Given
        val items = emptyList<String>()
        val selectedItem = ""

        // When
        composeRule.setContent {
            WellBiteAgePicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = {},
                modifier = Modifier.testTag("agePicker")
            )
        }

        // Then
        // Component should render without crashing even with empty list
        composeRule.onNodeWithTag("agePicker").assertExists()
    }

    @Test
    fun testInvalidSelectedItem() {
        // Given
        val items = listOf("18", "19", "20")
        val selectedItem = "25" // Not in the list

        // When
        composeRule.setContent {
            WellBiteAgePicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = {},
                modifier = Modifier.testTag("agePicker")
            )
        }

        // Then
        // Should default to first item when selectedItem is invalid
        composeRule.onNodeWithText("18").assertIsDisplayed()
    }

    @Test
    fun testScrollingBehavior() {
        // Given
        val items = listOf("18", "19", "20", "21", "22")
        var currentSelection = "20"

        // When
        composeRule.setContent {
            var selectedItem by remember { mutableStateOf(currentSelection) }

            WellBiteAgePicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = {
                    selectedItem = it
                    currentSelection = it
                },
                modifier = Modifier.testTag("agePicker")
            )
        }

        // Then
        // Test scrolling up
        composeRule.onNodeWithTag("agePicker").performTouchInput {
            swipeUp()
        }
        composeRule.waitForIdle()

        // Test scrolling down
        composeRule.onNodeWithTag("agePicker").performTouchInput {
            swipeDown()
        }
        composeRule.waitForIdle()

        // Verify we can return to original selection
        composeRule.onNodeWithText("20").assertIsDisplayed()
    }

    @Test
    fun testLargeItemsList() {
        // Given
        val items = (1..100).map { it.toString() }
        val selectedItem = "50"

        // When
        composeRule.setContent {
            WellBiteAgePicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = {},
                modifier = Modifier.testTag("agePicker")
            )
        }

        // Then
        // Component should handle large lists without issues
        composeRule.onNodeWithTag("agePicker").assertExists()
        composeRule.onNodeWithText(selectedItem).assertIsDisplayed()
    }

    @Test
    fun testCustomTextModifier() {
        // Given
        val items = listOf("18", "19", "20")
        val selectedItem = "19"

        // When
        composeRule.setContent {
            WellBiteAgePicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = {},
                textModifier = Modifier.size(100.dp, 50.dp),
                modifier = Modifier.testTag("agePicker")
            )
        }

        // Then
        // Component should render with custom text size modifier
        composeRule.onNodeWithTag("agePicker").assertExists()
        composeRule.onNodeWithText(selectedItem).assertExists()
    }

    @Test
    fun testThemeIntegration() {
        // Given
        val items = listOf("18", "19", "20")
        val selectedItem = "19"

        // When
        composeRule.setContent {
            MaterialTheme {
                WellBiteAgePicker(
                    items = items,
                    selectedItem = selectedItem,
                    onItemSelected = {},
                    dividerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("agePicker")
                )
            }
        }

        // Then
        // Component should integrate with Material Theme
        composeRule.onNodeWithTag("agePicker").assertExists()
        composeRule.onNodeWithText(selectedItem).assertExists()
    }

    @Test
    fun testSelectionCallback() {
        // Given
        val items = listOf("18", "19", "20")
        val selectedItem = "19"
        var callbackInvoked = false
        var selectedValue = ""

        // When
        composeRule.setContent {
            WellBiteAgePicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = {
                    callbackInvoked = true
                    selectedValue = it
                },
                modifier = Modifier.testTag("agePicker")
            )
        }

        // Then
        // Scroll to trigger selection callback
        composeRule.onNodeWithTag("agePicker").performTouchInput {
            swipeUp()
        }
        composeRule.waitForIdle()

        // Assert callback was invoked and selection changed
        assert(callbackInvoked)
        assert(selectedValue.isNotEmpty())
    }
}
