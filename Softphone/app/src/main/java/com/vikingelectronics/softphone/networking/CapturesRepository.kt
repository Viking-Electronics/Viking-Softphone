package com.vikingelectronics.softphone.networking

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import com.vikingelectronics.softphone.accounts.SipAccount
import com.vikingelectronics.softphone.accounts.User
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.captures.LocalStorageCaptureTemplate
import com.vikingelectronics.softphone.dagger.UserScope
import com.vikingelectronics.softphone.storage.LocalCaptureDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.Result

interface CapturesRepository {
    suspend fun getExternalCaptures(storedCaptureUris: List<Uri>): Flow<Capture>
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

    private data class StorageReferenceMetadataHolder(val storageReference: StorageReference, var metadata: StorageMetadata, val shouldIgnoreStoredMetadataUri: Boolean)

    override suspend fun getExternalCaptures(storedCaptureUris: List<Uri>): Flow<Capture> {
        return storageRef.listAll()
            .await()
            .items
            .map {
                val metadata = it.metadata.await()
                val cloudStoredUri = metadata.getCustomMetadata("5514255221u1")?.toUri()
                val shouldIgnore = cloudStoredUri != null && cloudStoredUri !in storedCaptureUris
                if (shouldIgnore) removeUriMetadata(it)

                StorageReferenceMetadataHolder(it, metadata, shouldIgnore)
            }

            .asFlow()
            .transform {
                val cap = generateCaptureFromStorageRef(it)
                emit(cap)
        }
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
            offer(Result.success(true))
        }.addOnFailureListener {
            offer(Result.failure<Boolean>(it))
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
                    setCustomMetadata("5514255221u1", state.captureUri.toString())
                }
                capture.storageReference.updateMetadata(metadata).await()
            }
        }
    }

    //TODO: Get actual user
    private suspend fun generateCaptureFromStorageRef(holder: StorageReferenceMetadataHolder): Capture {
        val metadata = holder.metadata

        val cloudStoreUri = holder.metadata.getCustomMetadata("5514255221u1")?.toUri()
        val storedLocally = !(cloudStoreUri == null || holder.shouldIgnoreStoredMetadataUri)
        val uri: Uri = if (cloudStoreUri == null || holder.shouldIgnoreStoredMetadataUri)  {
            holder.storageReference.downloadUrl.await()
        } else cloudStoreUri

        val name = holder.storageReference.name
        val creationTimeMillis = metadata.creationTimeMillis
        val size = metadata.sizeBytes
        val type = metadata.contentType ?: ""

        val favorite = metadata.getCustomMetadata(FAVORITE_KEY).toBoolean()


        return Capture(name, uri, creationTimeMillis, size, type).apply {
            storageReference = holder.storageReference
            isFavorite = favorite
            isStoredLocally = storedLocally
        }
    }

    private suspend fun removeUriMetadata(reference: StorageReference) {
        val metadata = storageMetadata {
            setCustomMetadata("5514255221u1", null)
        }

        reference.updateMetadata(metadata).await()
    }
}