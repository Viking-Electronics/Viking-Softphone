package com.vikingelectronics.softphone.records.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikingelectronics.softphone.networking.RecordsRepository
import com.vikingelectronics.softphone.records.Record
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordsViewModel @Inject constructor (
    private val repository: RecordsRepository
): ViewModel(){


    var localRecords: List<Record> = mutableStateListOf<Record>()
        private set

    var externalRecords: List<Record> by mutableStateOf(listOf())
        private set

    init {
        fetchExternalRecords()
    }

    fun fetchExternalRecords() {
        viewModelScope.launch {
            repository.getExternalRecords().collect {
                externalRecords += it
            }
        }
    }

    fun favoriteRecord(record: Record) {
        viewModelScope.launch {
            val shouldBeFavorite = !record.isFavorite
            repository.updateFavorite(record.storageReference, shouldBeFavorite).collect {
                if (it.isSuccess) {
                    record.isFavorite = shouldBeFavorite
                } else TODO()
            }
        }
    }

    fun deleteRecord(record: Record) {

    }

    fun downloadRecord(record: Record) {

    }


}