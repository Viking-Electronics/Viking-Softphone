package com.vikingelectronics.softphone.activity.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(): ViewModel() {

    init {
        getAllStorageEntries()
    }

    var storageFileRefs: List<StorageReference> by mutableStateOf(listOf())
    var storageFileDLRefs: List<String> by mutableStateOf(listOf())

    fun getAllStorageEntries() {
        Firebase.storage.reference.listAll().addOnSuccessListener {
            it.items.forEach { ref ->
                storageFileRefs += ref
                viewModelScope.launch {
                    val thing = ref.downloadUrl.await().toString()
                    storageFileDLRefs += thing
                }
            }
        }
    }
}