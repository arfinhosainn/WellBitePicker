package com.wellbite.wellbite_picker.date_picker


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.stream.Collectors
import java.util.stream.Stream

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WellBiteWeekPicker(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    selectedBorderColor: Color,
    unSelectedBorderColor: Color,
    shape: Shape = RoundedCornerShape(25.dp),
    dayStyle: TextStyle = MaterialTheme.typography.bodySmall,
    dayOfMonthStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    selectedDateStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    onDateClickListener: (LocalDate) -> Unit
) {
    val dataSource = CalendarDataSource()
    var data by remember { mutableStateOf(dataSource.getData(lastSelectedDate = dataSource.today)) }

    Column(modifier = modifier.fillMaxSize()) {
//        Header(
//            data = data,
//            selectedDateStyle = selectedDateStyle,
//            onPrevClickListener = { startDate ->
//                val finalStartDate = startDate.minusDays(1)
//                data = dataSource.getData(
//                    startDate = finalStartDate, lastSelectedDate = data.selectedDate.date
//                )
//            },
//            onNextClickListener = { endDate ->
//                val finalStartDate = endDate.plusDays(2)
//                data = dataSource.getData(
//                    startDate = finalStartDate, lastSelectedDate = data.selectedDate.date
//                )
//            }
//        )
        Content(
            data = data, backgroundColor = backgroundColor,
            selectedBorderColor = selectedBorderColor,
            unSelectedBorderColor = unSelectedBorderColor,
            shape = shape,
            dayStyle = dayStyle,
            dayOfMonthStyle = dayOfMonthStyle,
        ) { date ->
            data = data.copy(
                selectedDate = date,
                visibleDates = data.visibleDates.map {
                    it.copy(
                        isSelected = it.date.isEqual(date.date)
                    )
                }
            )
            onDateClickListener(date.date)
        }
    }
}

//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//private fun Header(
//    data: CalendarUiModel,
//    selectedDateStyle: TextStyle,
//    onPrevClickListener: (LocalDate) -> Unit,
//    onNextClickListener: (LocalDate) -> Unit,
//) {
//    Row {
//        Text(
//            text = data.startDate.date.month.name.capitalize() + ", " + data.startDate.date.year,
//            modifier = Modifier
//                .weight(1f)
//                .align(Alignment.CenterVertically),
//            style = selectedDateStyle
//        )
//        Image(
//            modifier = Modifier
//                .size(24.dp)
//                .clickable {
//                    onPrevClickListener(data.startDate.date)
//                },
//            painter = painterResource(id = R.drawable.ic_back_calendar),
//            contentDescription = "Previous"
//        )
//        Image(
//            modifier = Modifier
//                .padding(start = 6.dp)
//                .size(24.dp)
//                .clickable {
//                    onNextClickListener(data.endDate.date)
//                },
//            painter = painterResource(id = R.drawable.ic_next_calendar),
//            contentDescription = "Next"
//        )
//    }
//}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun Content(
    data: CalendarUiModel,
    backgroundColor: Color,
    selectedBorderColor: Color,
    unSelectedBorderColor: Color,
    shape: Shape = RoundedCornerShape(15.dp),
    dayStyle: TextStyle = MaterialTheme.typography.bodySmall,
    dayOfMonthStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    onDateClickListener: (CalendarUiModel.Date) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(top = 20.dp)
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        repeat(data.visibleDates.size) { index ->
            ContentItem(
                date = data.visibleDates[index],
                backgroundColor = backgroundColor,
                selectedBorderColor = selectedBorderColor,
                unSelectedBorderColor = unSelectedBorderColor,
                shape = shape,
                dayStyle = dayStyle,
                dayOfMonthStyle = dayOfMonthStyle,
                onClickListener = onDateClickListener
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun RowScope.ContentItem(
    date: CalendarUiModel.Date,
    backgroundColor: Color,
    selectedBorderColor: Color,
    unSelectedBorderColor: Color,
    shape: Shape,
    dayStyle: TextStyle,
    dayOfMonthStyle: TextStyle,
    onClickListener: (CalendarUiModel.Date) -> Unit,
) {
    // Define the selected background color as #7fff6c
    val selectedBackgroundColor = Color(0xFF7FFF6C)

    Column(modifier = Modifier
        .padding(vertical =1.dp, horizontal = 5.dp)
        .noRippleEffect {
            onClickListener(date)
        }
        .weight(1f)
        .height(80.dp)
        .background(
            if (date.isSelected) selectedBackgroundColor else backgroundColor,
            shape
        )
        .border(
            if (date.isSelected) (1.2).dp else (0.5).dp,
            if (date.isSelected) selectedBorderColor else unSelectedBorderColor,
            shape
        )
        .padding(vertical = 10.dp, horizontal = 4.dp)
        .clip(shape),
        verticalArrangement = Arrangement.Center) {
        Text(
            text = date.day,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = dayStyle,
        )

        // Circle with date number
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(28.dp)
                .background(
                    color = Color.White,
                    shape = CircleShape
                )
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.date.dayOfMonth.toString(),
                style = dayOfMonthStyle,
            )
        }
    }
}

private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.ROOT
        ) else it.toString()
    }
}

class CalendarDataSource {

    val today: LocalDate
        @RequiresApi(Build.VERSION_CODES.O)
        get() {
            return LocalDate.now()
        }

    @RequiresApi(Build.VERSION_CODES.O)
    internal fun getData(startDate: LocalDate = today, lastSelectedDate: LocalDate): CalendarUiModel {
        val firstDayOfWeek = startDate.with(DayOfWeek.MONDAY)
        val endDayOfWeek = firstDayOfWeek.plusDays(7)
        val visibleDates = getDatesBetween(firstDayOfWeek, endDayOfWeek)
        return toUiModel(visibleDates, lastSelectedDate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDatesBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val numOfDays = ChronoUnit.DAYS.between(startDate, endDate)
        return Stream.iterate(startDate) { date ->
            date.plusDays(1)
        }
            .limit(numOfDays)
            .collect(Collectors.toList())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toUiModel(
        dateList: List<LocalDate>,
        lastSelectedDate: LocalDate
    ): CalendarUiModel {
        return CalendarUiModel(
            selectedDate = toItemUiModel(lastSelectedDate, true),
            visibleDates = dateList.map {
                toItemUiModel(it, it.isEqual(lastSelectedDate))
            },
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toItemUiModel(date: LocalDate, isSelectedDate: Boolean) = CalendarUiModel.Date(
        isSelected = isSelectedDate,
        isToday = date.isEqual(today),
        date = date,
    )
}

internal data class CalendarUiModel(
    val selectedDate: Date,
    val visibleDates: List<Date>
) {

    val startDate: Date = visibleDates.first()
    val endDate: Date = visibleDates.last()

    data class Date(
        val date: LocalDate,
        val isSelected: Boolean,
        val isToday: Boolean
    ) {
        @RequiresApi(Build.VERSION_CODES.O)
        val day: String = date.format(DateTimeFormatter.ofPattern("E"))
    }
}
// You need to define this R class in your project
// or import the correct resource files
//object R {
//    object drawable {
//        const val ic_back_calendar = android.R.drawable.ic_media_previous // Placeholder
//        const val ic_next_calendar = android.R.drawable.ic_media_next // Placeholder
//    }
//}



///*** Preview Code  Starts here. ***/
//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun HorizontalCalendarDemo() {
//    val context = LocalContext.current
//
//    WellBiteWeekSelector (
//        modifier = Modifier,
//        backgroundColor = Color(0xFFF1F3F5),
//        selectedBorderColor = Color(0xFF0C0A0A),
//        unSelectedBorderColor = Color(0xffDEDEDE),
//        dayStyle = TextStyle(
//            color = Color(0xFF000000),
//            fontSize = 13.sp,
//        ),
//        dayOfMonthStyle = TextStyle(
//            color = Color.Black,
//            fontSize = 16.sp,
//            fontWeight = FontWeight.SemiBold
//        ),
//        selectedDateStyle = TextStyle(
//            color = Color.Black,
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold,
//        ),
//        onDateClickListener = { date ->
//            Toast
//                .makeText(context, date.toString(), Toast.LENGTH_SHORT)
//                .show()
//        },
//    )
//}
/*** Preview Code  Ends here. ***/


//@RequiresApi(Build.VERSION_CODES.O)
//@Preview(showBackground = true)
//@Composable
//private fun PreviewWeeklyCalender() {
//    HorizontalCalendarDemo()
//
//}