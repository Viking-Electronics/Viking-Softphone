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
    private val dayNumberMap = mapOf(
        1 to "Su",
        2 to "M",
        3 to "T",
        4 to "W",
        5 to "Th",
        6 to "F",
        7 to "Sa"
    )

    fun activeDaysToString(): String = buildString {
        when(activeDays.sorted()) {
            EVERYDAY -> append("Everyday")
            WEEKENDS -> append("Weekends")
            WEEKDAYS -> append("Weekdays")
            else -> activeDays.sorted().forEach {
                append(dayNumberMap[it])
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
