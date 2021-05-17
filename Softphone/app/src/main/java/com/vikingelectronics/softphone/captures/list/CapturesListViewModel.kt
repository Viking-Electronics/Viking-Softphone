package com.vikingelectronics.softphone.captures.list

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.accounts.UserProvider
import com.vikingelectronics.softphone.networking.CapturesRepository
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.captures.LocalStorageCaptureTemplate
import com.vikingelectronics.softphone.dagger.UserComponentEntryPoint
import com.vikingelectronics.softphone.extensions.timber
import com.vikingelectronics.softphone.storage.LocalCaptureDataSource
import com.vikingelectronics.softphone.util.PermissionsManager
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CapturesListViewModel @Inject constructor (
    private val userProvider: UserProvider,
    private val permissionsManager: PermissionsManager,
): ViewModel() {

    private val repository: CapturesRepository = userProvider.userComponentEntryPoint.capturesRepository()

    private val localUris = mutableListOf<Uri>()

    val capturesList: Flow<PagingData<Capture>> = Pager(
        config = PagingConfig(8),
        initialKey = null,
        pagingSourceFactory = { CapturePagingSource(repository, localUris) }
    ).flow.cachedIn(viewModelScope)

    init {

        viewModelScope.launch {
            //TODO: this is a less than ideal solution to the permissions issue
            permissionsManager.requestPermissionForStorage(this) {
                repository.getStoredTemplates().transform<LocalStorageCaptureTemplate, Uri> {
                    it.uri
                }.collect {
                    localUris.add(it)
                }
            }
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

    suspend fun deleteCapture(capture: Capture, onSuccess: () -> Unit): Flow<Int> = repository.deleteCapture(capture).transform {
        if (it.isSuccess) {
            onSuccess()

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