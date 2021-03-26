package com.vikingelectronics.softphone.networking

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.storage.LocalCaptureDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import kotlin.Result

interface CapturesRepository {
    suspend fun getExternalCaptures(scope: CoroutineScope): Flow<Capture>
    suspend fun getStoredCaptures(): Flow<Capture>
    suspend fun updateFavorite(storageReference: StorageReference, shouldBeFavorite: Boolean): Flow<Result<Boolean>>
    suspend fun downloadCapture(capture: Capture): Flow<LocalCaptureDataSource.DownloadState>
}

class CapturesRepositoryImpl @Inject constructor(
    override val db: FirebaseFirestore,
    override val storage: FirebaseStorage,
    private val localCaptureSource: LocalCaptureDataSource,
    @ApplicationContext val context: Context,
): FirebaseRepository(), CapturesRepository {


    override suspend fun getExternalCaptures(scope: CoroutineScope): Flow<Capture> = channelFlow {
        initStorageRecord()
        storageRef?.listAll()
                ?.await()
                ?.items
                ?.forEach {
                    scope.launch {
                        it.metadata.await().apply {
                            if (getCustomMetadata("5514255221u1") == null) {
                                offer(generateCaptureFromStorageRef(it, this))
                            }
                        }
                    }
                }
        awaitClose()
    }

    override suspend fun getStoredCaptures(): Flow<Capture> = localCaptureSource.fetchCapturesFromStorage()

    override suspend fun updateFavorite(storageReference: StorageReference, shouldBeFavorite: Boolean): Flow<Result<Boolean>> {
        val metadata = storageMetadata {
            setCustomMetadata(FAVORITE_KEY, shouldBeFavorite.toString())
        }

        return storageReference.updateMetadata(metadata).emitResult()
    }

    //TODO: Get actual user
    override suspend fun downloadCapture(capture: Capture): Flow<LocalCaptureDataSource.DownloadState> {
        return localCaptureSource.saveCapture(capture).onEach { state ->
            if (state is LocalCaptureDataSource.DownloadState.Success) {
                val metadata = storageMetadata {
                    setCustomMetadata("5514255221u1", state.captureUri.toString())
                }
                capture.storageReference.updateMetadata(metadata).await()
            }
        }
    }

    //TODO: Get actual user
    private suspend fun generateCaptureFromStorageRef(reference: StorageReference, metadata: StorageMetadata): Capture {
//        val metadata = reference.metadata.await()

        val name = reference.name
        val uri = metadata.getCustomMetadata("5514255221u1")?.toUri() ?: reference.downloadUrl.await()
//        val downloadUrl = reference.downloadUrl.await()
//        val localUri = metadata.getCustomMetadata("5514255221u1")
        val creationTimeMillis = metadata.creationTimeMillis
        val size = metadata.sizeBytes
        val type = metadata.contentType ?: ""

        val favorite = metadata.getCustomMetadata(FAVORITE_KEY).toBoolean()


        return Capture(name, uri, creationTimeMillis, size, type).apply {
            storageReference = reference
            isFavorite = favorite
        }
    }
}