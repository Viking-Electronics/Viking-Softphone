package com.vikingelectronics.softphone.devices.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.networking.DeviceRepositoryImpl
import com.vikingelectronics.softphone.util.LinphoneManager
import com.vikingelectronics.softphone.util.PermissionsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    val linphoneManager: LinphoneManager,
    private val permissionsManager: PermissionsManager,
    private val repositoryImpl: DeviceRepositoryImpl
) : ViewModel() {

    init {
        getDevices()
    }


    var devices: List<Device> by mutableStateOf(listOf())
        private set

    private fun getDevices() {
        viewModelScope.launch {
            repositoryImpl.getDevices("5514255221u1").collect {
                devices += it
            }
        }
    }

    fun goLive(device: Device) {
        permissionsManager.requestPermissionsForAudio {
            linphoneManager.callDevice(viewModelScope, device)
        }
    }
}