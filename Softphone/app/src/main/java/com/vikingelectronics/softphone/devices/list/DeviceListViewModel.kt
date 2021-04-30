package com.vikingelectronics.softphone.devices.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikingelectronics.softphone.accounts.UserProvider
import com.vikingelectronics.softphone.dagger.UserComponentEntryPoint
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.networking.DeviceRepository
import com.vikingelectronics.softphone.util.LinphoneManager
import com.vikingelectronics.softphone.util.PermissionsManager
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    val linphoneManager: LinphoneManager,
    private val permissionsManager: PermissionsManager,
    private val userProvider: UserProvider
) : ViewModel() {

    private val repository: DeviceRepository
        get() = EntryPoints.get(userProvider.userComponent, UserComponentEntryPoint::class.java).deviceRepository()
    init {
        getDevices()
    }


    var devices: List<Device> by mutableStateOf(listOf())
        private set

    private fun getDevices() {
        viewModelScope.launch {
            repository.getDevices("5514255221u1").collect {
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