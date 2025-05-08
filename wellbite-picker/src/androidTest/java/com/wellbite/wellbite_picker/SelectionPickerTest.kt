//package com.wellbite.wellbite_picker
//
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.compose.ui.test.onNodeWithText
//import androidx.compose.ui.test.performClick
//import com.wellbite.wellbite_picker.wellbite_selector.SelectionPickerConfig
//import com.wellbite.wellbite_picker.wellbite_selector.WellBiteSelectionPicker
//import junit.framework.TestCase.assertEquals
//import org.junit.Rule
//import org.junit.Test
//
//@OptIn(ExperimentalMaterial3Api::class)
//class WellBiteSelectionPickerTest {
//
//    @get:Rule
//    val composeTestRule = createComposeRule()
//
//    private val customConfig = SelectionPickerConfig(
//        defaultSelection = listOf("Male", "Female", "Other"),
//        maxCustomSelection = 3
//    )
//
//    @Test
//    fun testDisplaysDefaultOptions() {
//        composeTestRule.setContent {
//            WellBiteSelectionPicker(
//                config = customConfig,
//                onSelected = {}
//            )
//        }
//
//        // Check that each default option is displayed
//        customConfig.defaultSelection.forEach { option ->
//            composeTestRule.onNodeWithText(option).assertExists()
//        }
//    }
//
//    @Test
//    fun testItemSelectionUpdatesUI() {
//        var selectedGender = ""
//        composeTestRule.setContent {
//            WellBiteSelectionPicker(
//                config = customConfig,
//                onSelected = { selectedGender = it },
//                onSelectedColor = Color.Red
//            )
//        }
//
//        // Click "Female" and check if callback was triggered
//        composeTestRule.onNodeWithText("Female").performClick()
//        composeTestRule.runOnIdle {
//            assertEquals("Female", selectedGender)
//        }
//    }
//
//    @Test
//    fun testCustomColorAppliedOnSelection() {
//        val selectedColor = Color.Green
//
//        composeTestRule.setContent {
//            WellBiteSelectionPicker(
//                config = customConfig,
//                onSelected = {},
//                onSelectedColor = selectedColor
//            )
//        }
//
//        // Click to select an item
//        composeTestRule.onNodeWithText("Male").performClick()
//
//        // Check if background color matches — not directly testable, but you can use semantics or tags to help
//        // To enable testing this, you should add a testTag in SelectionItem:
//        // Modifier.testTag("selection_${item}")
//
//        // Then:
//        // composeTestRule.onNodeWithTag("selection_Male").assertExists()
//    }
//
//    @Test
//    fun testLongListWithScroll() {
//        val longList = (1..50).map { "Item $it" }
//        composeTestRule.setContent {
//            WellBiteSelectionPicker(
//                config = SelectionPickerConfig(defaultSelection = longList),
//                onSelected = {}
//            )
//        }
//
//        // Scroll to the last item
//        composeTestRule.onNodeWithText("Item 50").assertExists()
//    }
//}
