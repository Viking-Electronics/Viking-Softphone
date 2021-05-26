package com.vikingelectronics.softphone.devices.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vikingelectronics.softphone.accounts.RepositoryProvider
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.util.LinphoneManager
import com.vikingelectronics.softphone.util.PermissionsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    val linphoneManager: LinphoneManager,
    private val permissionsManager: PermissionsManager,
    private val repositoryProvider: RepositoryProvider
) : ViewModel() {

    val devicesList: Flow<PagingData<Device>> = Pager(
        config = PagingConfig(8),
        initialKey = null,
        pagingSourceFactory = { DeviceListPagingSource(repositoryProvider.deviceRepository) }
    ).flow.cachedIn(viewModelScope)

    fun goLive(device: Device) {
        permissionsManager.requestPermissionsForAudio {
            linphoneManager.callDevice(device)
        }
    }
}