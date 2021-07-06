package com.vikingelectronics.shared.schedules

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class ScheduleTimeframe(
    val startHours: Int = 0,
    val startMinutes: Int = 0,
    val endHours: Int = 0,
    val endMinutes: Int = 0
) {

    constructor(start: LocalTime, end: LocalTime)
        : this(start.getHour(), start.getMinute(), end.getHour(), end.getMinute())

    constructor() : this(LocalTime.now(), LocalTime.now().plusHours(1))


    fun startLocalTime(): LocalTime = LocalTime.of(startHours, startMinutes)
    fun endLocalTime(): LocalTime = LocalTime.of(endHours, endMinutes)
    fun startTwelveHourFormat(): String = startLocalTime().format(TwelveHourTimeFormatter)
    fun endTwelveHourFormat(): String = endLocalTime().format(TwelveHourTimeFormatter)

    fun isAllDay(): Boolean = startLocalTime() == LocalTime.MIN && endLocalTime() == LocalTime.MAX.truncatedTo(ChronoUnit.MINUTES)

    companion object {
        val TwelveHourTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    }
}
