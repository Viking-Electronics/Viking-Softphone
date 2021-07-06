package com.vikingelectronics.shared.captures

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import com.vikingelectronics.shared.accounts.SipAccount
import com.vikingelectronics.shared.accounts.User
import com.vikingelectronics.softphone.captures.LocalStorageCaptureTemplate
import com.vikingelectronics.softphone.dagger.UserScope
import com.vikingelectronics.softphone.storage.LocalCaptureDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.Result

data class CapturesListPaginationHolder(val captures: List<Capture>, val pageToken: String?)

interface CapturesRepository {
    suspend fun getExternalCaptures(amountToFetch: Int, storedCaptureUris: List<Uri>, pageToken: String?, ): CapturesListPaginationHolder?
    suspend fun getStoredTemplates(): Flow<LocalStorageCaptureTemplate>
    suspend fun updateFavorite(capture: Capture, shouldBeFavorite: Boolean): Flow<Result<Boolean>>
    suspend fun deleteCapture(capture: Capture): Flow<Result<Boolean>>
    suspend fun downloadCapture(capture: Capture): Flow<LocalCaptureDataSource.DownloadState>
}

@UserScope
class CapturesRepositoryImpl @Inject constructor(
    override val db: FirebaseFirestore,
    override val storage: FirebaseStorage,
    override val user: User,
    override val sipAccount: SipAccount,
    private val localCaptureSource: LocalCaptureDataSource,
    @ApplicationContext val context: Context,
): FirebaseRepository(), CapturesRepository {


    override suspend fun getExternalCaptures(amountToFetch: Int, storedCaptureUris: List<Uri>, pageToken: String?): CapturesListPaginationHolder? {
        val listTask = pageToken?.let { storageRef.list(amountToFetch, it) } ?: storageRef.list(amountToFetch)

        val listResult = listTask.await()

        val captures = listResult.items.map { storageReference ->
            withContext(Dispatchers.IO) {
                val metadata = storageReference.metadata.await()
                val cloudStoredUri = metadata.getCustomMetadata(user.username)?.toUri()
                val shouldIgnore = cloudStoredUri != null && cloudStoredUri !in storedCaptureUris
                if (shouldIgnore) removeUriMetadata(storageReference)

                generateCaptureFromStorageRef(storageReference, metadata, shouldIgnore)
            }
        }

        return CapturesListPaginationHolder(captures, listResult.pageToken)
    }

    override suspend fun getStoredTemplates(): Flow<LocalStorageCaptureTemplate> = localCaptureSource.fetchLocalCaptureTemplates()

    override suspend fun updateFavorite(capture: Capture, shouldBeFavorite: Boolean): Flow<Result<Boolean>> {
        if (capture.isStoredLocally) localCaptureSource.updateFavoriteOfCapture(capture, shouldBeFavorite)

        val metadata = storageMetadata {
            setCustomMetadata(FAVORITE_KEY, shouldBeFavorite.toString())
        }

        return capture.storageReference.updateMetadata(metadata).emitResult()
    }

    override suspend fun deleteCapture(capture: Capture): Flow<Result<Boolean>> = callbackFlow {
        capture.storageReference.delete().addOnSuccessListener {
            trySend(Result.success(true))
        }.addOnFailureListener {
            trySend(Result.failure<Boolean>(it))
        }.addOnCompleteListener {
            close()
        }
        awaitClose()
    }

    //TODO: Get actual user
    override suspend fun downloadCapture(capture: Capture): Flow<LocalCaptureDataSource.DownloadState> {
        return localCaptureSource.saveCapture(capture).onEach { state ->
            if (state is LocalCaptureDataSource.DownloadState.Success) {
                val metadata = storageMetadata {
                    setCustomMetadata(user.username, state.captureUri.toString())
                }
                capture.storageReference.updateMetadata(metadata).await()
            }
        }
    }

    //TODO: Get actual user
    private suspend fun generateCaptureFromStorageRef(
        storageReference: StorageReference,
        metadata: StorageMetadata,
        shouldIgnoreStoredMetadataUri: Boolean
    ): Capture {
        val cloudStoreUri = metadata.getCustomMetadata(user.username)?.toUri()
        val storedLocally = !(cloudStoreUri == null || shouldIgnoreStoredMetadataUri)
        val uri: Uri = if (cloudStoreUri == null || shouldIgnoreStoredMetadataUri)  {
            storageReference.downloadUrl.await()
        } else cloudStoreUri

        val name = storageReference.name
        val creationTimeMillis = metadata.creationTimeMillis
        val size = metadata.sizeBytes
        val type = metadata.contentType ?: ""

        val favorite = metadata.getCustomMetadata(FAVORITE_KEY).toBoolean()


        return Capture(name, uri, creationTimeMillis, size, type).apply {
            this.storageReference = storageReference
            isFavorite = favorite
            isStoredLocally = storedLocally
        }
    }

    private suspend fun removeUriMetadata(reference: StorageReference) {
        val metadata = storageMetadata {
            setCustomMetadata(user.username, null)
        }

        reference.updateMetadata(metadata).await()
    }
}