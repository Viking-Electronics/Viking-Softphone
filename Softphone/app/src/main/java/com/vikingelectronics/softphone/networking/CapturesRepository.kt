package com.vikingelectronics.softphone.networking

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.storage.LocalCaptureDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import kotlin.Result

interface CapturesRepository {
    suspend fun getExternalCaptures(): Flow<Capture>
    suspend fun updateFavorite(storageReference: StorageReference, shouldBeFavorite: Boolean): Flow<Result<Boolean>>
    suspend fun downloadCapture(capture: Capture): Flow<LocalCaptureDataSource.DownloadState>
}

class CapturesRepositoryImpl @Inject constructor(
    override val db: FirebaseFirestore,
    override val storage: FirebaseStorage,
    val localCaptureSource: LocalCaptureDataSource,
    @ApplicationContext val context: Context,
): FirebaseRepository(), CapturesRepository {


    override suspend fun getExternalCaptures(): Flow<Capture> = flow {
        initStorageRecord()
        storageRef?.listAll()
                ?.await()
                ?.items
                ?.forEach { emit(generateCaptureFromStorageRef(it)) }
    }

    override suspend fun updateFavorite(storageReference: StorageReference, shouldBeFavorite: Boolean): Flow<Result<Boolean>> {
        val metadata = storageMetadata {
            setCustomMetadata(FAVORITE_KEY, shouldBeFavorite.toString())
        }

        return storageReference.updateMetadata(metadata).emitResult()
    }

    override suspend fun downloadCapture(capture: Capture): Flow<LocalCaptureDataSource.DownloadState> = localCaptureSource.saveCapture(capture)

    private suspend fun generateCaptureFromStorageRef(reference: StorageReference): Capture {
        val metadata = reference.metadata.await()

        val name = reference.name
        val downloadUrl = reference.downloadUrl.await()
        val id = metadata.getCustomMetadata(UUID_KEY) ?: generateAndSetUUID(reference)
        val creationTimeMillis = metadata.creationTimeMillis
        val size = metadata.sizeBytes
        val type = metadata.contentType ?: ""

        val favorite = metadata.getCustomMetadata(FAVORITE_KEY).toBoolean()


        return Capture(name, id, downloadUrl, creationTimeMillis,  size, type).apply {
            storageReference = reference
            isFavorite = favorite
        }
    }

    private suspend fun generateAndSetUUID(reference: StorageReference): String {
        val id = UUID.randomUUID().toString()
        val metadata = storageMetadata {
            setCustomMetadata(UUID_KEY, id)
        }

        reference.updateMetadata(metadata).await()

        return id
    }


    fun getStoredRecords() {

    }

//    private fun convertTimestamp(metadata: StorageMetadata): String {
//
//        val time = metadata.creationTimeMillis
//        val now: Long = Clock.System.now().toEpochMilliseconds()
//
//        return "Captured ${DateUtils.getRelativeTimeSpanString(time, now, 1000L)}"
//    }
}