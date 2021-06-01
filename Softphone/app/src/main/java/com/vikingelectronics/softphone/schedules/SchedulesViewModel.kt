package com.vikingelectronics.softphone.schedules

import android.view.View
import androidx.lifecycle.ViewModel
import com.dpro.widgets.OnWeekdaysChangeListener
import com.vikingelectronics.softphone.extensions.timber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class SchedulesViewModel @Inject constructor(

): ViewModel(), OnWeekdaysChangeListener {

    val isGloballySnoozed = MutableStateFlow(false)
    val isInMultiselect = MutableStateFlow(false)

    //    override fun onDaysSelectionChange(dayPickerView: DayPickerView?, selectedDays: BooleanArray?) {
////        selectedDays.
//    }
    override fun onChange(view: View?, clickedDayOfWeek: Int, selectedDays: MutableList<Int>?) {
        selectedDays.toString().timber()
    }

    fun globalSnoozeClicked(isChecked: Boolean) {
        isGloballySnoozed.value = isChecked
    }

    fun addSchedule() {

    }

    fun deleteSelectedSchedules() {

    }
}