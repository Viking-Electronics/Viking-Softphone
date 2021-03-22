package com.vikingelectronics.softphone.captures.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikingelectronics.softphone.networking.CapturesRepository
import com.vikingelectronics.softphone.captures.Capture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CapturesListViewModel @Inject constructor (
    private val repository: CapturesRepository
): ViewModel(){


    var localCaptures: List<Capture> = mutableStateListOf<Capture>()
        private set

    var externalCaptures: List<Capture> by mutableStateOf(listOf())
        private set

    init {
        fetchExternalRecords()
    }

    fun fetchExternalRecords() {
        viewModelScope.launch {
            repository.getExternalCaptures().collect {
                externalCaptures += it
            }
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

    }


}