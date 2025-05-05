package com.wellbite.wellbite_picker.weight_picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Data class to represent weight configuration and selection
 */
data class WeightPickerConfig(
    val minWeight: Float = 80f,
    val maxWeight: Float = 300f,
    val step: Float = 0.5f,
    val weightUnit: WeightUnit = WeightUnit.POUNDS
)

/**
 * Represents weight measurement units
 */
enum class WeightUnit(val label: String) {
    POUNDS("lbs"),
    KILOGRAMS("kg")
}

/**
 * State holder for the weight picker
 */
class WeightPickerState(
    initialWeight: Float,
    val config: WeightPickerConfig
) {
    private val _weight =
        mutableFloatStateOf(initialWeight.coerceIn(config.minWeight, config.maxWeight))
    val weight: Float get() = _weight.floatValue

    internal val selectedIndex: Int
        get() = ((weight - config.minWeight) / config.step).toInt()

    fun updateWeight(newWeight: Float) {
        _weight.floatValue = newWeight.coerceIn(config.minWeight, config.maxWeight)
    }
}

/**
 * Remember a WeightPickerState with the given initial configuration
 */
@Composable
fun rememberWeightPickerState(
    initialWeight: Float = 110.5f,
    config: WeightPickerConfig = WeightPickerConfig()
): WeightPickerState {
    return remember {
        WeightPickerState(initialWeight, config)
    }
}

/**
 * Enhanced WellBiteWeightPicker that uses a state holder pattern for better data management
 */
@Composable
fun WellBiteWeightPicker(
    modifier: Modifier = Modifier,
    state: WeightPickerState = rememberWeightPickerState(),
    onWeightSelected: (Float) -> Unit = {}
) {
    val config = state.config
    val totalSteps = ((config.maxWeight - config.minWeight) / config.step).toInt()

    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = state.selectedIndex.coerceIn(0, totalSteps)
    )

    val coroutineScope = rememberCoroutineScope()

    // Format weight display based on selected unit and step precision
    val displayWeight = remember(state.weight, config.step) {
        if (config.step < 1f) {
            // Show decimal places for fractional steps
            val decimalDigits = when {
                config.step <= 0.01f -> 2
                config.step <= 0.1f -> 1
                else -> 1
            }
            String.format("%.${decimalDigits}f", state.weight)
        } else {
            // No decimal places for whole number steps
            state.weight.toInt().toString()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Weight display
        Text(
            text = "$displayWeight ${config.weightUnit.label}",
            style = TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Ruler
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            // Right half shadow
            Box(
                modifier = Modifier
                    .width(LocalConfiguration.current.screenWidthDp.dp / 2)
                    .height(60.dp)
                    .background(Color(0x11000000))
                    .align(Alignment.CenterEnd)
            )

            // Center line indicator
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
                    .background(Color.Black)
                    .align(Alignment.Center)
            )

            // Ruler ticks
            LazyRow(
                state = lazyListState,
                contentPadding = PaddingValues(horizontal = (LocalConfiguration.current.screenWidthDp / 2).dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationX = 180f
                    }
            ) {
                items(totalSteps + 1) { index ->
                    val weight = config.minWeight + (index * config.step)
                    val isMajorTick = weight % 10 == 0f
                    val isMinorTick = weight % 1 == 0f

                    Box(
                        modifier = Modifier.width(10.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(
                                    when {
                                        isMajorTick -> 40.dp
                                        isMinorTick -> 30.dp
                                        else -> 20.dp
                                    }
                                )
                                .background(Color.Gray.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }
    }

    // Update selected weight when scrolling stops
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val center = lazyListState.layoutInfo.viewportStartOffset +
                        lazyListState.layoutInfo.viewportSize.width / 2
                val selectedItem = visibleItems.minByOrNull {
                    abs((it.offset + it.size / 2) - center)
                }

                selectedItem?.let {
                    val newIndex = it.index
                    val newWeight = config.minWeight + (newIndex * config.step)
                    val constrainedWeight = newWeight.coerceIn(config.minWeight, config.maxWeight)

                    // Update state
                    state.updateWeight(constrainedWeight)

                    // Notify callback
                    onWeightSelected(constrainedWeight)
                }
            }
        }
    }

    // Synchronize external state changes with the list position
    LaunchedEffect(state.weight) {
        val targetIndex = state.selectedIndex
        if (lazyListState.firstVisibleItemIndex != targetIndex && !lazyListState.isScrollInProgress) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(targetIndex)
            }
        }
    }
}
