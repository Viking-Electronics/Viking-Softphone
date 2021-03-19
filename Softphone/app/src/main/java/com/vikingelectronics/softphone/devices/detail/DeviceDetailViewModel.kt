package com.vikingelectronics.softphone.devices.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.networking.DeviceRepository
import com.vikingelectronics.softphone.networking.DeviceRepositoryImpl
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


    fun getActivityFeedForDevice(device: Device) {
        viewModelScope.launch {
            repository.getDeviceActivityList(device).collect {
                if (!activityList.contains(it)) activityList += it
            }
        }
    }
}