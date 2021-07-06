package com.vikingelectronics.softphone.devices.list

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.vikingelectronics.shared.devices.Device
import com.vikingelectronics.softphone.networking.DeviceRepository

class DeviceListPagingSource(
    private val repository: DeviceRepository
): PagingSource<DocumentSnapshot, Device>() {

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Device>): DocumentSnapshot? = null

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Device> {
        return try {
            repository.getDevices(params.key).let {
                LoadResult.Page(
                    data = it.entries,
                    nextKey = it.index,
                    prevKey = params.key
                )
            } ?: LoadResult.Error(Throwable("Error fetching results"))

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}