package com.vikingelectronics.softphone.captures.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikingelectronics.softphone.networking.CapturesRepository
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.storage.LocalCaptureDataSource
import com.vikingelectronics.softphone.util.PermissionsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collect
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
//        fetchExternalCaptures()
//        fetchStoredCaptures()
        fetchCaptures()
    }

    fun fetchExternalCaptures() {
        viewModelScope.launch {
            repository.getExternalCaptures(this).collect {
                capturesList += it
            }
        }
    }

    fun fetchStoredCaptures() {
        viewModelScope.launch {
            repository.getStoredCaptures().collect {  }
        }
    }

    fun fetchCaptures() {
        viewModelScope.launch {
            val list = mutableListOf<Capture>()

            val external = async {
                repository.getExternalCaptures(this).collect {
                    list.add(it)
                }
            }
            val internal = async {
                repository.getStoredCaptures().collect {
                    list.add(it)
                }
            }
            awaitAll(internal, external)

            list.sortBy { it.creationTimeMillis }
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