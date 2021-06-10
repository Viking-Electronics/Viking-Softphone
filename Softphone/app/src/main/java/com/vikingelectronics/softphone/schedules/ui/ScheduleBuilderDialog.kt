package com.vikingelectronics.softphone.schedules.ui

import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import com.dpro.widgets.OnWeekdaysChangeListener
import com.dpro.widgets.WeekdaysPicker
import com.vanpra.composematerialdialogs.*
import com.vanpra.composematerialdialogs.datetime.timepicker.timepicker
import com.vikingelectronics.softphone.schedules.data.Schedule
import com.vikingelectronics.softphone.schedules.data.ScheduleTimeframe
import java.time.LocalTime

sealed class ScheduleBuilderStep {
    object DayPicker: ScheduleBuilderStep()
    object StartTime: ScheduleBuilderStep()
    object EndTime: ScheduleBuilderStep()

    fun getNext(): ScheduleBuilderStep? {
        return when(this) {
            DayPicker -> StartTime
            StartTime -> EndTime
            EndTime -> null
        }
    }
    fun getPrevious(): ScheduleBuilderStep? {
        return when(this) {
            DayPicker -> null
            StartTime -> DayPicker
            EndTime -> StartTime
        }
    }
}

@Composable
fun MaterialDialog.ScheduleBuilderDialog(
    scheduleForEdit: Schedule = Schedule.DEFAULT,
    scheduleProduced: (Schedule) -> Unit
) {
    val titleText =if (scheduleForEdit == Schedule.DEFAULT) "New Schedule" else "Edit Schedule"
    val schedule by remember { mutableStateOf(scheduleForEdit) }

    var step by remember { mutableStateOf<ScheduleBuilderStep>(ScheduleBuilderStep.DayPicker) }
    var selectedDays by remember { mutableStateOf(schedule.activeDays) }
    var startTime by remember { mutableStateOf(schedule.timeframe.startLocalTime()) }
    var endTime by remember { mutableStateOf(schedule.timeframe.endLocalTime()) }
    var allDay by remember { mutableStateOf(schedule.allDay) }

    val dayListener = OnWeekdaysChangeListener { _, _, days ->
        selectedDays = days
    }

    fun complete() {
        val producedSchedule = schedule.copy(
            activeDays = selectedDays,
            timeframe = ScheduleTimeframe(startTime, endTime),
            allDay = allDay
        )

        scheduleProduced(producedSchedule)
    }

    if(selectedDays.isEmpty()) disablePositiveButton() else enablePositiveButton()


    title(titleText)

    when(step) {
        ScheduleBuilderStep.DayPicker -> {
            message("Select days for schedule")
            customView {
                AndroidView (
                    { context ->
                        WeekdaysPicker(context).apply {
                            setOnWeekdaysChangeListener(dayListener)
                            setSelectedDays(selectedDays)
                            setEditable(true)
                            showWeekend  = true
                        }
                    }
                )
            }
            buttons {
                positiveButton(
                    "Next",
                    disableDismiss = true,
                    onClick = {
                        step.getNext()?.let {
                            step = it
                        }
                    }
                )
                negativeButton("Cancel")
            }
        }
        ScheduleBuilderStep.StartTime -> {
            timepicker(
                title = "Select Start Time",
                initialTime = startTime,
                onTimeChange = { localTime -> startTime = localTime }
            )
            buttons {
                positiveButton(
                    "Next",
                    disableDismiss = true,
                    onClick = {
                        step.getNext()?.let {
                            step = it
                        }
                    }
                )
                button(
                    "Back",
                    onClick = {
                        step.getPrevious()?.let {
                            step = it
                        }
                    }
                )
                button(
                    "All Day",
                    onClick = {
                        allDay = true
                        startTime = LocalTime.MIN
                        endTime = LocalTime.MAX

                        complete()
                    }
                )
            }
        }
        ScheduleBuilderStep.EndTime -> {
            timepicker(
                title = "Select End Time",
                initialTime = endTime,
                onTimeChange = { localTime -> endTime = localTime }
            )
            buttons {
                positiveButton(
                    "Done",
                    onClick = ::complete
                )
                button(
                    "Back",
                    onClick = {
                        step.getPrevious()?.let {
                            step = it
                        }
                    }
                )
            }
        }
    }
}