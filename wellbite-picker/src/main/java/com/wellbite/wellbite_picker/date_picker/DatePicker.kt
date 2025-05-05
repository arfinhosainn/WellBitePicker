package com.wellbite.wellbite_picker.date_picker

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.AnimationScope
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellBiteDatePicker(
    modifier: Modifier,
    title: String = "DAY, MONTH & YEAR PICKER",
    doneLabel: String = "Done",
    titleStyle: TextStyle = LocalTextStyle.current,
    doneLabelStyle: TextStyle = LocalTextStyle.current,
    startDate: LocalDate = LocalDate.now(),
    minDate: LocalDate = LocalDate.MIN,
    maxDate: LocalDate = LocalDate.MAX,
    yearsRange: IntRange? = IntRange(1922, 2122),
    size: DpSize = DpSize(256.dp, 128.dp),
    rowCount: Int = 3,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    textColor: Color = LocalContentColor.current,
    selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
    onDoneClick: (d: LocalDate) -> Unit = {},
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Column(modifier) {
        Box(Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = titleStyle,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = doneLabel,
                style = doneLabelStyle,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .align(Alignment.CenterEnd)
                    .noRippleEffect {
                        onDoneClick(selectedDate)
                    }
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            thickness = (0.5).dp,
            color = Color.LightGray
        )
        CustomMonthYearPicker(
            textColor = textColor,
            selectorProperties = selectorProperties,
            rowCount = rowCount,
            size = size,
            startDate = startDate,
            minDate = minDate,
            maxDate = maxDate,
            yearsRange = yearsRange,
            textStyle = textStyle,
            modifier = Modifier.padding(top = 12.dp, bottom = 50.dp)
        ) { snappedDate ->
            selectedDate = snappedDate
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun CustomMonthYearPicker(
    modifier: Modifier = Modifier,
    startDate: LocalDate = LocalDate.now(),
    minDate: LocalDate = LocalDate.MIN,
    maxDate: LocalDate = LocalDate.MAX,
    yearsRange: IntRange? = IntRange(1922, 2122),
    size: DpSize = DpSize(256.dp, 128.dp),
    rowCount: Int = 3,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    textColor: Color = LocalContentColor.current,
    selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
    onSnappedDate: (snappedDate: LocalDate) -> Unit = {}
) {
    DefaultWheelMonthYearPicker(
        modifier = modifier,
        startDate = startDate,
        minDate = minDate,
        maxDate = maxDate,
        yearsRange = yearsRange,
        size = size,
        rowCount = rowCount,
        textStyle = textStyle,
        textColor = textColor,
        selectorProperties = selectorProperties,
        onSnappedDate = { snappedDate ->
            onSnappedDate(snappedDate.snappedLocalDate)
            snappedDate.snappedIndex
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun DefaultWheelMonthYearPicker(
    modifier: Modifier = Modifier,
    startDate: LocalDate = LocalDate.now(),
    minDate: LocalDate = LocalDate.MIN,
    maxDate: LocalDate = LocalDate.MAX,
    yearsRange: IntRange? = IntRange(1922, 2122),
    size: DpSize = DpSize(256.dp, 128.dp),
    rowCount: Int = 3,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    textColor: Color = LocalContentColor.current,
    selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
    onSnappedDate: (snappedDate: SnappedDate) -> Int? = { _ -> null }
) {
    var snappedDate by remember { mutableStateOf(startDate) }

    val months = (1..12).map {
        Month(
            text = DateFormatSymbols().months[it - 1],
            value = it,
            index = it - 1
        )
    }


    val days = (1..LocalDate.of(snappedDate.year, snappedDate.monthValue, 1).lengthOfMonth()).map {
        Day(
            text = it.toString(),
            value = it,
            index = it - 1
        )
    }



    val years = yearsRange?.map {
        Year(
            text = it.toString(),
            value = it,
            index = yearsRange.indexOf(it)
        )
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (selectorProperties.enabled().value) {
            HorizontalDivider(
                modifier = Modifier.padding(bottom = (size.height / rowCount)),
                thickness = (0.5).dp,
                color = selectorProperties.borderColor().value
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = (size.height / rowCount)),
                thickness = (0.5).dp,
                color = selectorProperties.borderColor().value
            )
        }
        Row {
            Spacer(modifier = Modifier.weight(0.8f))
            WheelTextPicker(
                modifier = Modifier.weight(1f),
                startIndex = months.find { it.value == startDate.monthValue }?.index ?: 0,
                size = DpSize(
                    width = if (yearsRange == null) size.width / 2 else size.width / 3,
                    height = size.height
                ),
                texts = months.map { it.text },
                rowCount = rowCount,
                style = textStyle,
                color = textColor,
                contentAlignment = Alignment.CenterStart
            ) { snappedIndex ->
                val newMonth = months.find { it.index == snappedIndex }?.value

                newMonth?.let {
                    val newDate = snappedDate.withMonth(newMonth)
                    if (!newDate.isBefore(minDate) && !newDate.isAfter(maxDate)) {
                        snappedDate = newDate
                    }

                    val newIndex = months.find { it.value == snappedDate.monthValue }?.index
                    newIndex?.let {
                        onSnappedDate(
                            SnappedDate.Month(
                                localDate = snappedDate,
                                index = newIndex
                            )
                        )?.let { return@WheelTextPicker it }
                    }
                }

                return@WheelTextPicker months.find { it.value == snappedDate.monthValue }?.index
            }
            days.let { days ->
                WheelTextPicker(
                    modifier = Modifier.weight(0.8f),
                    startIndex = days.find { it.value == startDate.year }?.index ?: 0,
                    size = DpSize(
                        width = size.width / 3,
                        height = size.height
                    ),
                    texts = days.map { it.text },
                    rowCount = rowCount,
                    style = textStyle,
                    color = textColor
                ) { snappedIndex ->
                    val newDay = days.find { it.index == snappedIndex }?.value
                    newDay?.let {
                        val newDate = snappedDate.withDayOfMonth(newDay)
                        if (!newDate.isBefore(minDate) && !newDate.isAfter(maxDate)) {
                            snappedDate = newDate
                        }

                        val newIndex = days.find { it.value == snappedDate.dayOfYear }?.index
                        newIndex?.let {
                            onSnappedDate(
                                SnappedDate.Days(
                                    localDate = snappedDate,
                                    index = newIndex
                                )
                            )?.let { return@WheelTextPicker it }
                        }
                    }

                    return@WheelTextPicker days.find { it.value == snappedDate.dayOfYear }?.index
                }
            }
            years?.let { years ->
                WheelTextPicker(
                    modifier = Modifier.weight(1f),
                    startIndex = years.find { it.value == startDate.year }?.index ?: 0,
                    size = DpSize(
                        width = size.width / 3,
                        height = size.height
                    ),
                    texts = years.map { it.text },
                    rowCount = rowCount,
                    style = textStyle,
                    color = textColor
                ) { snappedIndex ->
                    val newYear = years.find { it.index == snappedIndex }?.value

                    newYear?.let {
                        val newDate = snappedDate.withYear(newYear)
                        if (!newDate.isBefore(minDate) && !newDate.isAfter(maxDate)) {
                            snappedDate = newDate
                        }

                        val newIndex = years.find { it.value == snappedDate.year }?.index
                        newIndex?.let {
                            onSnappedDate(
                                SnappedDate.Year(
                                    localDate = snappedDate,
                                    index = newIndex
                                )
                            )?.let { return@WheelTextPicker it }
                        }
                    }

                    return@WheelTextPicker years.find { it.value == snappedDate.year }?.index
                }
            }
            Spacer(modifier = Modifier.weight(.8f))
        }
    }
}

@Composable
internal fun WheelTextPicker(
    modifier: Modifier = Modifier,
    startIndex: Int = 0,
    size: DpSize = DpSize(128.dp, 128.dp),
    texts: List<String>,
    rowCount: Int,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = LocalContentColor.current,
    contentAlignment: Alignment = Alignment.Center,
    onScrollFinished: (snappedIndex: Int) -> Int? = { null },
) {
    WheelPicker(
        modifier = modifier,
        startIndex = startIndex,
        count = texts.size,
        rowCount = rowCount,
        size = size,
        onScrollFinished = onScrollFinished,
        texts = texts,
        style = style,
        color = color,
        contentAlignment = contentAlignment
    )
}

@OptIn(ExperimentalSnapperApi::class)
@Composable
internal fun WheelPicker(
    modifier: Modifier = Modifier,
    startIndex: Int = 0,
    count: Int,
    rowCount: Int,
    size: DpSize = DpSize(128.dp, 128.dp),
    onScrollFinished: (snappedIndex: Int) -> Int? = { null },
    texts: List<String>,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = LocalContentColor.current,
    contentAlignment: Alignment = Alignment.Center,
) {
    val lazyListState = rememberLazyListState(startIndex)
    val snapperLayoutInfo = rememberLazyListSnapperLayoutInfo(lazyListState = lazyListState)
    val isScrollInProgress = lazyListState.isScrollInProgress

    LaunchedEffect(isScrollInProgress, count) {
        if (!isScrollInProgress) {
            onScrollFinished(calculateSnappedItemIndex(snapperLayoutInfo) ?: startIndex)?.let {
                lazyListState.scrollToItem(it)
            }
        }
    }

    val topBottomFade = Brush.verticalGradient(
        0f to Color.Transparent,
        0.3f to Color.Black,
        0.7f to Color.Black,
        1f to Color.Transparent
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .height(size.height)
                .fillMaxWidth()
                .fadingEdge(topBottomFade),
            state = lazyListState,
            contentPadding = PaddingValues(vertical = size.height / rowCount * ((rowCount - 1) / 2)),
            flingBehavior = rememberSnapperFlingBehavior(
                lazyListState = lazyListState
            )
        ) {
            items(count) { index ->
                Box(
                    modifier = Modifier
                        .height(size.height / rowCount)
                        .fillMaxWidth()
                        .alpha(
                            calculateAnimatedAlpha(
                                lazyListState = lazyListState,
                                snapperLayoutInfo = snapperLayoutInfo,
                                index = index,
                                rowCount = rowCount
                            )
                        ),
                    contentAlignment = contentAlignment
                ) {
                    Text(
                        text = texts[index],
                        style = style,
                        color = if (calculateSnappedItemIndex(snapperLayoutInfo) == index) color else Color.Black,
                        maxLines = 1,
                        fontSize = if (calculateSnappedItemIndex(snapperLayoutInfo) == index) 18.sp else 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSnapperApi::class)
private fun calculateSnappedItemIndex(snapperLayoutInfo: SnapperLayoutInfo): Int? {
    var currentItemIndex = snapperLayoutInfo.currentItem?.index

    if (snapperLayoutInfo.currentItem?.offset != 0) {
        if (currentItemIndex != null) {
            currentItemIndex++
        }
    }
    return currentItemIndex
}

internal fun Modifier.noRippleEffect(
    onClick: () -> Unit
) = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

internal fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

@RequiresApi(Build.VERSION_CODES.O)
internal fun dateToString(snappedDate: LocalDate, givenFormat: String = "MMM dd, yyyy"): String {
    val formatter = DateTimeFormatter.ofPattern(givenFormat)
    return snappedDate.format(formatter)
}

@RequiresOptIn(message = "Snapper is experimental. The API may be changed in the future.")
@Retention(AnnotationRetention.BINARY)
internal annotation class ExperimentalSnapperApi

@ExperimentalSnapperApi
internal object SnapperFlingBehaviorDefaults {
    val SpringAnimationSpec: AnimationSpec<Float> = spring(stiffness = 400f)
    val SnapIndex: (SnapperLayoutInfo, startIndex: Int, targetIndex: Int) -> Int =
        { _, _, targetIndex -> targetIndex }
}

@ExperimentalSnapperApi
@Composable
internal fun rememberSnapperFlingBehavior(
    layoutInfo: SnapperLayoutInfo,
    decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
    springAnimationSpec: AnimationSpec<Float> = SnapperFlingBehaviorDefaults.SpringAnimationSpec,
    snapIndex: (SnapperLayoutInfo, startIndex: Int, targetIndex: Int) -> Int = SnapperFlingBehaviorDefaults.SnapIndex,
): SnapperFlingBehavior = remember(
    layoutInfo,
    decayAnimationSpec,
    springAnimationSpec,
    snapIndex,
) {
    SnapperFlingBehavior(
        layoutInfo = layoutInfo,
        decayAnimationSpec = decayAnimationSpec,
        springAnimationSpec = springAnimationSpec,
        snapIndex = snapIndex,
    )
}

@ExperimentalSnapperApi
@Composable
internal fun rememberSnapperFlingBehavior(
    lazyListState: LazyListState,
    snapOffsetForItem: (layoutInfo: SnapperLayoutInfo, item: SnapperLayoutItemInfo) -> Int = SnapOffsets.Center,
    decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
    springAnimationSpec: AnimationSpec<Float> = SnapperFlingBehaviorDefaults.SpringAnimationSpec,
    snapIndex: (SnapperLayoutInfo, startIndex: Int, targetIndex: Int) -> Int = SnapperFlingBehaviorDefaults.SnapIndex,
): SnapperFlingBehavior = rememberSnapperFlingBehavior(
    layoutInfo = rememberLazyListSnapperLayoutInfo(lazyListState, snapOffsetForItem),
    decayAnimationSpec = decayAnimationSpec,
    springAnimationSpec = springAnimationSpec,
    snapIndex = snapIndex,
)

@ExperimentalSnapperApi
@Composable
internal fun rememberLazyListSnapperLayoutInfo(
    lazyListState: LazyListState,
    snapOffsetForItem: (layoutInfo: SnapperLayoutInfo, item: SnapperLayoutItemInfo) -> Int = SnapOffsets.Center,
): LazyListSnapperLayoutInfo = remember(lazyListState, snapOffsetForItem) {
    LazyListSnapperLayoutInfo(
        lazyListState = lazyListState,
        snapOffsetForItem = snapOffsetForItem,
    )
}

@ExperimentalSnapperApi
internal abstract class SnapperLayoutInfo {
    abstract val startScrollOffset: Int
    abstract val endScrollOffset: Int
    abstract val visibleItems: Sequence<SnapperLayoutItemInfo>
    abstract val currentItem: SnapperLayoutItemInfo?
    abstract val totalItemsCount: Int
    abstract fun distanceToIndexSnap(index: Int): Int
    abstract fun canScrollTowardsStart(): Boolean
    abstract fun canScrollTowardsEnd(): Boolean
}

abstract class SnapperLayoutItemInfo {
    abstract val index: Int
    abstract val offset: Int
    abstract val size: Int

    override fun toString(): String {
        return "SnapperLayoutItemInfo(index=$index, offset=$offset, size=$size)"
    }
}

@ExperimentalSnapperApi
private object SnapOffsets {
    val Start: (SnapperLayoutInfo, SnapperLayoutItemInfo) -> Int =
        { layout, _ -> layout.startScrollOffset }

    val Center: (SnapperLayoutInfo, SnapperLayoutItemInfo) -> Int = { layout, item ->
        layout.startScrollOffset + (layout.endScrollOffset - layout.startScrollOffset - item.size) / 2
    }

    val End: (SnapperLayoutInfo, SnapperLayoutItemInfo) -> Int = { layout, item ->
        layout.endScrollOffset - item.size
    }
}

@ExperimentalSnapperApi
internal class SnapperFlingBehavior(
    private val layoutInfo: SnapperLayoutInfo,
    private val decayAnimationSpec: DecayAnimationSpec<Float>,
    private val springAnimationSpec: AnimationSpec<Float>,
    private val snapIndex: (SnapperLayoutInfo, startIndex: Int, targetIndex: Int) -> Int,
) : FlingBehavior {

    private var animationTarget: Int? by mutableStateOf(null)

    override suspend fun ScrollScope.performFling(
        initialVelocity: Float
    ): Float {
        if (!layoutInfo.canScrollTowardsStart() || !layoutInfo.canScrollTowardsEnd()) {
            return initialVelocity
        }

        val initialItem = layoutInfo.currentItem ?: return initialVelocity

        val targetIndex = snapIndex(
            layoutInfo,
            if (initialVelocity < 0) initialItem.index + 1 else initialItem.index,
            initialItem.index,
        ).also {
            require(it in 0 until layoutInfo.totalItemsCount)
        }

        return flingToIndex(index = targetIndex, initialVelocity = initialVelocity)
    }

    private suspend fun ScrollScope.flingToIndex(
        index: Int,
        initialVelocity: Float,
    ): Float {
        val initialItem = layoutInfo.currentItem ?: return initialVelocity

        if (initialItem.index == index && layoutInfo.distanceToIndexSnap(initialItem.index) == 0) {
            return consumeVelocityIfNotAtScrollEdge(initialVelocity)
        }

        var velocityLeft = initialVelocity

        if (decayAnimationSpec.canDecayBeyondCurrentItem(initialVelocity, initialItem)) {
            velocityLeft = performDecayFling(
                initialItem = initialItem,
                targetIndex = index,
                initialVelocity = velocityLeft,
            )
        }

        val currentItem = layoutInfo.currentItem ?: return initialVelocity
        if (currentItem.index != index || layoutInfo.distanceToIndexSnap(index) != 0) {
            velocityLeft = performSpringFling(
                initialItem = currentItem,
                targetIndex = index,
                initialVelocity = velocityLeft,
            )
        }

        return consumeVelocityIfNotAtScrollEdge(velocityLeft)
    }

    private suspend fun ScrollScope.performDecayFling(
        initialItem: SnapperLayoutItemInfo,
        targetIndex: Int,
        initialVelocity: Float,
        flingThenSpring: Boolean = true,
    ): Float {
        if (initialItem.index == targetIndex && layoutInfo.distanceToIndexSnap(initialItem.index) == 0) {
            return consumeVelocityIfNotAtScrollEdge(initialVelocity)
        }

        var velocityLeft = initialVelocity
        var lastValue = 0f

        val canSpringThenFling = flingThenSpring && abs(targetIndex - initialItem.index) >= 2

        try {
            animationTarget = targetIndex

            AnimationState(
                initialValue = 0f,
                initialVelocity = initialVelocity,
            ).animateDecay(decayAnimationSpec) {
                val delta = value - lastValue
                val consumed = scrollBy(delta)
                lastValue = value
                velocityLeft = velocity

                if (abs(delta - consumed) > 0.5f) {
                    cancelAnimation()
                }

                val currentItem = layoutInfo.currentItem
                if (currentItem == null) {
                    cancelAnimation()
                    return@animateDecay
                }

                if (isRunning && canSpringThenFling) {
                    if (velocity > 0 && currentItem.index == targetIndex - 1) {
                        cancelAnimation()
                    } else if (velocity < 0 && currentItem.index == targetIndex) {
                        cancelAnimation()
                    }
                }

                if (isRunning && performSnapBackIfNeeded(
                        currentItem,
                        targetIndex,
                        ::scrollBy
                    )
                ) {
                    cancelAnimation()
                }
            }
        } finally {
            animationTarget = null
        }

        return velocityLeft
    }

    private suspend fun ScrollScope.performSpringFling(
        initialItem: SnapperLayoutItemInfo,
        targetIndex: Int,
        initialVelocity: Float = 0f,
    ): Float {
        var velocityLeft = when {
            targetIndex > initialItem.index && initialVelocity > 0 -> initialVelocity
            targetIndex <= initialItem.index && initialVelocity < 0 -> initialVelocity
            else -> 0f
        }
        var lastValue = 0f

        try {
            animationTarget = targetIndex

            AnimationState(
                initialValue = lastValue,
                initialVelocity = velocityLeft,
            ).animateTo(
                targetValue = layoutInfo.distanceToIndexSnap(targetIndex).toFloat(),
                animationSpec = springAnimationSpec,
            ) {
                val delta = value - lastValue
                val consumed = scrollBy(delta)
                lastValue = value
                velocityLeft = velocity

                val currentItem = layoutInfo.currentItem
                if (currentItem == null) {
                    cancelAnimation()
                    return@animateTo
                }

                if (performSnapBackIfNeeded(currentItem, targetIndex, ::scrollBy)) {
                    cancelAnimation()
                } else if (abs(delta - consumed) > 0.5f) {
                    cancelAnimation()
                }
            }
        } finally {
            animationTarget = null
        }

        return velocityLeft
    }

    private fun AnimationScope<Float, AnimationVector1D>.performSnapBackIfNeeded(
        currentItem: SnapperLayoutItemInfo,
        targetIndex: Int,
        scrollBy: (pixels: Float) -> Float,
    ): Boolean {
        SnapperLog.d {
            "scroll tick. " +
                    "vel:$velocity, " +
                    "current item: $currentItem"
        }

        val snapBackAmount = calculateSnapBack(velocity, currentItem, targetIndex)

        if (snapBackAmount != 0) {
            SnapperLog.d {
                "Scrolled past item. " +
                        "vel:$velocity, " +
                        "current item: $currentItem} " +
                        "target:$targetIndex"
            }
            scrollBy(snapBackAmount.toFloat())
            return true
        }

        return false
    }

    private fun DecayAnimationSpec<Float>.canDecayBeyondCurrentItem(
        velocity: Float,
        currentItem: SnapperLayoutItemInfo,
    ): Boolean {
        if (velocity.absoluteValue < 0.5f) return false

        val flingDistance = calculateTargetValue(0f, velocity)

        return if (velocity < 0) {
            flingDistance <= layoutInfo.distanceToIndexSnap(currentItem.index)
        } else {
            flingDistance >= layoutInfo.distanceToIndexSnap(currentItem.index + 1)
        }
    }


    private fun calculateSnapBack(
        initialVelocity: Float,
        currentItem: SnapperLayoutItemInfo,
        targetIndex: Int,
    ): Int = when {
        initialVelocity > 0 && currentItem.index >= targetIndex -> {
            layoutInfo.distanceToIndexSnap(currentItem.index)
        }

        initialVelocity < 0 && currentItem.index <= targetIndex - 1 -> {
            layoutInfo.distanceToIndexSnap(currentItem.index + 1)
        }

        else -> 0
    }


    private fun consumeVelocityIfNotAtScrollEdge(velocity: Float): Float {
        if (velocity < 0 && !layoutInfo.canScrollTowardsStart()) {
            return velocity
        } else if (velocity > 0 && !layoutInfo.canScrollTowardsEnd()) {
            return velocity
        }
        return 0f
    }
}

@ExperimentalSnapperApi
internal class LazyListSnapperLayoutInfo internal constructor(
    private val lazyListState: LazyListState,
    private val snapOffsetForItem: (layoutInfo: SnapperLayoutInfo, item: SnapperLayoutItemInfo) -> Int,
) : SnapperLayoutInfo() {

    override val startScrollOffset: Int = 0

    override val endScrollOffset: Int
        get() = lazyListState.layoutInfo.let { it.viewportEndOffset - it.afterContentPadding }

    private val itemCount: Int get() = lazyListState.layoutInfo.totalItemsCount

    override val totalItemsCount: Int
        get() = lazyListState.layoutInfo.totalItemsCount

    override val currentItem: SnapperLayoutItemInfo? by derivedStateOf {
        visibleItems.lastOrNull { it.offset <= snapOffsetForItem(this, it) }
    }

    override val visibleItems: Sequence<SnapperLayoutItemInfo>
        get() = lazyListState.layoutInfo.visibleItemsInfo.asSequence()
            .map(::LazyListSnapperLayoutItemInfo)

    override fun distanceToIndexSnap(index: Int): Int {
        val itemInfo = visibleItems.firstOrNull { it.index == index }
        if (itemInfo != null) {
            return itemInfo.offset - snapOffsetForItem(this, itemInfo)
        }

        val currentItem = currentItem ?: return 0
        return ((index - currentItem.index) * estimateDistancePerItem()).roundToInt() +
                currentItem.offset -
                snapOffsetForItem(this, currentItem)
    }

    override fun canScrollTowardsStart(): Boolean {
        return lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.let {
            it.index > 0 || it.offset < startScrollOffset
        } ?: false
    }

    override fun canScrollTowardsEnd(): Boolean {
        return lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.let {
            it.index < itemCount - 1 || (it.offset + it.size) > endScrollOffset
        } ?: false
    }

    private fun calculateItemSpacing(): Int = with(lazyListState.layoutInfo) {
        if (visibleItemsInfo.size >= 2) {
            val first = visibleItemsInfo[0]
            val second = visibleItemsInfo[1]
            second.offset - (first.size + first.offset)
        } else 0
    }

    private fun estimateDistancePerItem(): Float = with(lazyListState.layoutInfo) {
        if (visibleItemsInfo.isEmpty()) return -1f

        val minPosView = visibleItemsInfo.minByOrNull { it.offset } ?: return -1f
        val maxPosView = visibleItemsInfo.maxByOrNull { it.offset + it.size } ?: return -1f

        val start = min(minPosView.offset, maxPosView.offset)
        val end = max(minPosView.offset + minPosView.size, maxPosView.offset + maxPosView.size)

        return when (val distance = end - start) {
            0 -> -1f
            else -> (distance + calculateItemSpacing()) / visibleItemsInfo.size.toFloat()
        }
    }
}

private class LazyListSnapperLayoutItemInfo(
    private val lazyListItem: LazyListItemInfo,
) : SnapperLayoutItemInfo() {
    override val index: Int get() = lazyListItem.index
    override val offset: Int get() = lazyListItem.offset
    override val size: Int get() = lazyListItem.size
}


private data class Day(
    val text: String,
    val value: Int,
    val index: Int
)


data class Month(
    val text: String,
    val value: Int,
    val index: Int
)

data class Year(
    val text: String,
    val value: Int,
    val index: Int
)

interface SelectorProperties {
    @Composable
    fun enabled(): State<Boolean>

    @Composable
    fun borderColor(): State<Color>
}

@Immutable
internal class DefaultSelectorProperties(
    private val enabled: Boolean,
    private val color: Color,
) : SelectorProperties {

    @Composable
    override fun enabled(): State<Boolean> {
        return rememberUpdatedState(enabled)
    }

    @Composable
    override fun borderColor(): State<Color> {
        return rememberUpdatedState(color)
    }
}

internal sealed class SnappedDate(val snappedLocalDate: LocalDate, val snappedIndex: Int) {
    data class Month(val localDate: LocalDate, val index: Int) : SnappedDate(localDate, index)
    data class Year(val localDate: LocalDate, val index: Int) : SnappedDate(localDate, index)
    data class Days(val localDate: LocalDate, val index: Int) : SnappedDate(localDate, index)
}

internal object WheelPickerDefaults {
    @Composable
    fun selectorProperties(
        enabled: Boolean = true,
        color: Color = MaterialTheme.colorScheme.primary.copy(0.7f),
    ): SelectorProperties =
        DefaultSelectorProperties(
            enabled = enabled,
            color = color,
        )
}

internal object SnapperLog {
    inline fun d(tag: String = "SnapperFlingBehavior", message: () -> String) {
        Log.d(tag, message())
    }
}


@OptIn(ExperimentalSnapperApi::class)
@Composable
internal fun calculateAnimatedAlpha(
    lazyListState: LazyListState,
    snapperLayoutInfo: SnapperLayoutInfo,
    index: Int,
    rowCount: Int
): Float {
    val distanceToIndexSnap = snapperLayoutInfo.distanceToIndexSnap(index).absoluteValue
    val layoutInfo = remember { derivedStateOf { lazyListState.layoutInfo } }.value
    val viewPortHeight = layoutInfo.viewportSize.height.toFloat()
    val singleViewPortHeight = viewPortHeight / rowCount

    return if (distanceToIndexSnap in 0..singleViewPortHeight.toInt()) {
        1.2f - (distanceToIndexSnap / singleViewPortHeight)
    } else {
        0.2f
    }
}
