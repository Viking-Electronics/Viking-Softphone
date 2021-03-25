package com.vikingelectronics.softphone.storage

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.vikingelectronics.softphone.captures.Capture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject


typealias ImageMedia = MediaStore.Images.Media

class LocalCaptureDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    sealed class DownloadState {
        object Success: DownloadState()
        class Downloading(val progress: Int): DownloadState()
        class Failure(val error: Throwable): DownloadState()
    }


    private val resolver = context.contentResolver

    private val imagesCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else MediaStore.Images.Media.EXTERNAL_CONTENT_URI


    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun saveCapture(capture: Capture): Flow<DownloadState> = callbackFlow {

        val newCaptureDetails = ContentValues().apply {
            put(ImageMedia._ID, capture.id)
            put(ImageMedia.DISPLAY_NAME, capture.name)
            put(ImageMedia.MIME_TYPE, capture.type)
            put(ImageMedia.IS_FAVORITE, capture.isFavorite)
            put(ImageMedia.SIZE, capture.sizeInBytes)
            put(ImageMedia.DATE_TAKEN, capture.creationTimeMillis)
            put(ImageMedia.DISPLAY_NAME, capture.name)
            put(ImageMedia.IS_PENDING, 1)
        }

        val captureUri = resolver.insert(imagesCollection, newCaptureDetails) ?: return@callbackFlow

        capture.storageReference.getStream { _, stream ->
            resolver.openOutputStream(captureUri)?.use { oStream ->
                stream.use { iStream ->
                    iStream.copyTo(oStream)
                }
                oStream.flush()
            }
        }.addOnProgressListener {
            val percentage = (100f * it.bytesTransferred / it.totalByteCount).toInt()
            offer(DownloadState.Downloading(percentage))
            Timber.d("Transfered percentage: $percentage")
        }.addOnSuccessListener {
            Timber.d("Success")
            newCaptureDetails.apply {
                clear()
                put(ImageMedia.IS_PENDING, 0)
            }
            resolver.update(captureUri, newCaptureDetails, null, null)
        }.addOnFailureListener {
            offer(DownloadState.Failure(it))
        }.addOnCompleteListener {
            offer(DownloadState.Success)
        }

        awaitClose()
    }
}