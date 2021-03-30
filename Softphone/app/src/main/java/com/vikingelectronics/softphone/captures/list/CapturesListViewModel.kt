package com.vikingelectronics.softphone.captures.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikingelectronics.softphone.networking.CapturesRepository
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.captures.LocalStorageCaptureTemplate
import com.vikingelectronics.softphone.storage.LocalCaptureDataSource
import com.vikingelectronics.softphone.util.PermissionsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CapturesListViewModel @Inject constructor (
    private val repository: CapturesRepository,
    private val permissionsManager: PermissionsManager,
): ViewModel(){

    var capturesList: List<Capture> by mutableStateOf(listOf())
        private set

    init {
        fetchCaptures()
    }


    //TODO: Clean this method up
    fun fetchCaptures() {
        viewModelScope.launch {
            val list = mutableListOf<Capture>()
            val localUriStrings = mutableListOf<LocalStorageCaptureTemplate>()
            repository.getStoredTemplates().flowOn(IO).collect {
                localUriStrings.add(it)
            }

            repository.getExternalCaptures(localUriStrings.map { it.uri }).flowOn(IO).collect {
                list.add(it)
            }

            list.sortByDescending { it.creationTimeMillis }
            capturesList += list
        }
    }

    fun favoriteRecord(capture: Capture) {
        viewModelScope.launch {
            val shouldBeFavorite = !capture.isFavorite
            repository.updateFavorite(capture.storageReference, shouldBeFavorite).collect {
                if (it.isSuccess) {
                    capture.isFavorite = shouldBeFavorite
                } else TODO()
            }
        }
    }

    fun deleteRecord(capture: Capture) {

    }

    fun downloadRecord(capture: Capture) {
        permissionsManager.requestPermissionForStorage {
            viewModelScope.launch {
                repository.downloadCapture(capture).collect { state ->
                    when(state) {
                        is LocalCaptureDataSource.DownloadState.Success -> {}
                        is LocalCaptureDataSource.DownloadState.Downloading -> {}
                        is LocalCaptureDataSource.DownloadState.Failure -> {}
                    }
                }
            }
        }
    }


}