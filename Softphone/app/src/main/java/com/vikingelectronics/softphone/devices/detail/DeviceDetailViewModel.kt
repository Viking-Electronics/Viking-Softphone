package com.vikingelectronics.softphone.devices.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.networking.DeviceRepository
import com.vikingelectronics.softphone.networking.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceDetailViewModel @Inject constructor(
    private val repository: DeviceRepository
): ViewModel() {


    var activityList: List<ActivityEntry> by mutableStateOf(listOf())
        private set

    var activityText: Int by mutableStateOf(R.string.empty_string_res)
        private set


    fun getActivityFeedForDevice(device: Device) {
        viewModelScope.launch {
            repository.getDeviceActivityList(device).collect {
                when(it) {
                    is FirebaseRepository.ListState.Success -> {
                        activityList += it.list
                        activityText = if (activityList.isEmpty()) R.string.no_previous_activity else R.string.previous_activity
                    }
                    is FirebaseRepository.ListState.Loading -> {
                        activityText = R.string.loading_previous_activity
                    }
                    is FirebaseRepository.ListState.Failure -> {
                        activityText = R.string.previous_activity_load_failure
                    }
                }
            }
        }
    }
}