package com.vikingelectronics.softphone.captures.list

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.networking.CapturesRepository
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.captures.LocalStorageCaptureTemplate
import com.vikingelectronics.softphone.extensions.timber
import com.vikingelectronics.softphone.storage.LocalCaptureDataSource
import com.vikingelectronics.softphone.util.PermissionsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
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

    suspend fun favoriteCapture(capture: Capture): Flow<Int> {
        val shouldBeFavorite = !capture.isFavorite
        return repository.updateFavorite(capture, shouldBeFavorite).transform {
             val stringRes = if (it.isSuccess) {
                capture.isFavorite = shouldBeFavorite
                if (shouldBeFavorite) R.string.cap_favorite_success else R.string.cap_unfavorite_success
            } else {
                if (shouldBeFavorite) R.string.cap_favorite_failure else R.string.cap_unfavorite_failure
            }

            emit(stringRes)
        }
    }

    suspend fun deleteCapture(capture: Capture): Flow<Int> = repository.deleteCapture(capture).transform {
        if (it.isSuccess) {
            val newList = capturesList.toMutableList()
            newList.remove(capture)
            capturesList = newList

            emit(R.string.cap_delete_success)
        } else emit(R.string.cap_delete_failure)
    }

    suspend fun downloadCapture(capture: Capture): Flow<Int> {
//         permissionsManager.requestPermissionForStorage(viewModelScope) {
            return repository.downloadCapture(capture).transform { state ->
                when(state) {
                    is LocalCaptureDataSource.DownloadState.Success ->  {
                        capture.isStoredLocally = true
                        emit(R.string.cap_download_success)
                    }
                    is LocalCaptureDataSource.DownloadState.Downloading -> {
                        capture.downloadProgress = state.progress.timber()
                    }
                    is LocalCaptureDataSource.DownloadState.Failure -> emit(R.string.cap_download_failure)
                }
            }
//        }
    }
}