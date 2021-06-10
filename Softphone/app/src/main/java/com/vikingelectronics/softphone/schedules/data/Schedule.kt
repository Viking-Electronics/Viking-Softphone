package com.vikingelectronics.softphone.schedules.data

import com.google.firebase.firestore.DocumentId
import com.vikingelectronics.softphone.networking.DEFAULT_FIREBASE_ID

data class Schedule(
    @DocumentId val id: String = DEFAULT_FIREBASE_ID,
    val activeDays: List<Int> = listOf(),
    val timeframe: ScheduleTimeframe = ScheduleTimeframe(),
    val allDay: Boolean = false,
    val enabled: Boolean = true
) {

    private data class NumberToDayString(val key: Int, val abbreviation: String, val fullDayString: String)
    private val dayNumberMap = listOf(
        NumberToDayString(1, "Su", "Sundays"),
        NumberToDayString(2, "M", "Mondays"),
        NumberToDayString(3, "Tu", "Tuesdays"),
        NumberToDayString(4, "W", "Wednesdays"),
        NumberToDayString(5, "Th", "Thursdays"),
        NumberToDayString(6, "F", "Fridays"),
        NumberToDayString(7, "Sa", "Saturdays")
    )

    fun activeDaysToString(): String = buildString {
        val sorted = activeDays.sorted()
        when {
            sorted == EVERYDAY -> append("Everyday")
            sorted == WEEKENDS -> append("Weekends")
            sorted == WEEKDAYS -> append("Weekdays")
            activeDays.size < 3 -> sorted.forEach {
                append(dayNumberMap[it - 1].fullDayString)
                if (activeDays.last() != it) append(" and ")
            }
            else -> sorted.forEach {
                append(dayNumberMap[it - 1].abbreviation)
                if (activeDays.last() != it) append(" - ")
            }
        }

    }
    fun timeframeToDisplayString(): String = buildString {
        append(timeframe.startTwelveHourFormat())
        append(" - ")
        append(timeframe.endTwelveHourFormat())
    }

    companion object {
        val EVERYDAY = listOf(1, 2, 3, 4, 5, 6, 7)
        val WEEKENDS = listOf(1, 7)
        val WEEKDAYS = listOf(2, 3, 4, 5, 6)
    }
}
