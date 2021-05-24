package com.vikingelectronics.softphone.devices.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vikingelectronics.softphone.accounts.UserProvider
import com.vikingelectronics.softphone.call.CallDirection
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.captures.list.CapturePagingSource
import com.vikingelectronics.softphone.dagger.UserComponentEntryPoint
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.extensions.setParcelableAndNavigate
import com.vikingelectronics.softphone.navigation.Screen
import com.vikingelectronics.softphone.networking.DeviceRepository
import com.vikingelectronics.softphone.util.LinphoneManager
import com.vikingelectronics.softphone.util.PermissionsManager
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    val linphoneManager: LinphoneManager,
    private val permissionsManager: PermissionsManager,
    userProvider: UserProvider
) : ViewModel() {

    private val repository: DeviceRepository = userProvider.userComponentEntryPoint.deviceRepository()

    val devicesList: Flow<PagingData<Device>> = Pager(
        config = PagingConfig(8),
        initialKey = null,
        pagingSourceFactory = { DeviceListPagingSource(repository) }
    ).flow.cachedIn(viewModelScope)

    fun goLive(device: Device, navController: NavController) {
        permissionsManager.requestPermissionsForAudio {
            linphoneManager.callDevice(device)
            navController.setParcelableAndNavigate(Screen.Secondary.Call, CallDirection.Outgoing(device))
        }
    }
}