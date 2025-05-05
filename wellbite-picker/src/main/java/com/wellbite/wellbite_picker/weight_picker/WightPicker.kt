package com.wellbite.wellbite_picker.weight_picker

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun WeightPicker(
    modifier: Modifier = Modifier,
    initialWeight: Float = 110.5f,
    minWeight: Float = 80f,
    maxWeight: Float = 300f,
    step: Float = 0.5f,
    onWeightSelected: (Float) -> Unit = {}
) {
    val totalSteps = ((maxWeight - minWeight) / step).toInt()
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = ((initialWeight - minWeight) / step).toInt()
            .coerceIn(0, totalSteps)
    )
    val selectedWeight = remember { mutableFloatStateOf(initialWeight) }
    val selectedIndex = remember { mutableIntStateOf(((initialWeight - minWeight) / step).toInt()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Weight display
        Text(
            text = "${selectedWeight.value.toInt()}.${((selectedWeight.value * 10).toInt() % 10)} lbs",
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
                flingBehavior = rememberSnapFlingBehavior(lazyListState),
                contentPadding = PaddingValues(horizontal = (LocalConfiguration.current.screenWidthDp / 2).dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationX = 180f
                    }
            ) {
                items(totalSteps + 1) { index ->
                    val weight = minWeight + (index * step)
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
                val center =
                    lazyListState.layoutInfo.viewportStartOffset + lazyListState.layoutInfo.viewportSize.width / 2
                val selectedItem = visibleItems.minByOrNull {
                    abs((it.offset + it.size / 2) - center)
                }

                selectedItem?.let {
                    val newIndex = it.index
                    val newWeight = minWeight + (newIndex * step)
                    selectedIndex.intValue = newIndex
                    selectedWeight.floatValue = newWeight.coerceIn(minWeight, maxWeight)
                    onWeightSelected(selectedWeight.floatValue)
                }
            }
        }
    }
}


@Composable
fun WeightPickerScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        WeightPicker(
            initialWeight = 80.5f,
            minWeight = 0f,
            maxWeight = 300f,
            step = 0.5f,
            onWeightSelected = { selectedWeight ->
                // Handle selected weight
            }
        )
    }
}

