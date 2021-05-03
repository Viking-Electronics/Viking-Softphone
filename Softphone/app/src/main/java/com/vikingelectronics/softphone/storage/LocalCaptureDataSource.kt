package com.vikingelectronics.softphone.storage

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.firebase.storage.StorageReference
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.captures.LocalStorageCaptureTemplate
import com.vikingelectronics.softphone.extensions.timber
import com.vikingelectronics.softphone.extensions.toInt
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject


typealias ImageMedia = MediaStore.Images.Media

class LocalCaptureDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    sealed class DownloadState {
        class Success(val captureUri: Uri): DownloadState()
        class Downloading(val progress: Float): DownloadState()
        sealed class Failure(open val error: Throwable): DownloadState() {
            class DownloadFailure(override val error: Throwable): Failure(error)
            class InsufficientSpace(override val error: Throwable): Failure(error)
        }
    }


    private val resolver = context.contentResolver

    private val imagesCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    private val imageQueryProjection = arrayOf(
        ImageMedia._ID,
        ImageMedia.DISPLAY_NAME,
        ImageMedia.MIME_TYPE,
        ImageMedia.IS_FAVORITE,
        ImageMedia.SIZE,
        ImageMedia.DISPLAY_NAME
    )
    private val imageQuerySortOrder = "${ImageMedia.DATE_ADDED} DESC"


    //TODO: Figure out why this doesn't work on any other api than 30
    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun saveCapture(capture: Capture): Flow<DownloadState> = callbackFlow {

        val newCaptureDetails = ContentValues().apply {
            put(ImageMedia.DISPLAY_NAME, capture.name)
            put(ImageMedia.MIME_TYPE, capture.type)
            put(ImageMedia.IS_FAVORITE, capture.isFavorite.toInt())
            put(ImageMedia.SIZE, capture.sizeInBytes)
            put(ImageMedia.IS_PENDING, 1)
        }

        val captureUri = resolver.insert(imagesCollection, newCaptureDetails) ?: return@callbackFlow

        capture.storageReference.getStream { _, stream ->
            try {
                resolver.openOutputStream(captureUri)?.use { oStream ->
                    stream.use { iStream ->
                        iStream.copyTo(oStream)
                    }
                    oStream.flush()
                }
            } catch (e: IOException) {
                offer(DownloadState.Failure.InsufficientSpace(e))
                resolver.delete(captureUri, null, null)
                close()
            }
        }.addOnProgressListener {

            val percentage = it.bytesTransferred.toFloat() / it.totalByteCount.toFloat().timber("Transferred percentage:")
            offer(DownloadState.Downloading(percentage))

        }.addOnSuccessListener {

            Timber.d("Success")
            newCaptureDetails.apply {
                clear()
                put(ImageMedia.IS_PENDING, 0)
            }
            resolver.update(captureUri, newCaptureDetails, null, null)

        }.addOnFailureListener {

            resolver.delete(captureUri, null, null)

        }.addOnCompleteListener {

            val state = it.exception?.let { exception -> DownloadState.Failure.DownloadFailure(exception) } ?: DownloadState.Success(captureUri)
            offer(state)
            channel.close()

        }

        awaitClose()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun fetchLocalCaptureTemplates(): Flow<LocalStorageCaptureTemplate> = callbackFlow {
        resolver.query(
            imagesCollection,
            imageQueryProjection,
            null,
            null,
            imageQuerySortOrder)?.use { cursor ->

            val idColumn = cursor.getColumnIndex(ImageMedia._ID)
            val nameColumn = cursor.getColumnIndex(ImageMedia.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn).timber()
                val name = cursor.getString(nameColumn)

                val uri = ContentUris.withAppendedId(imagesCollection, id)
                val template = LocalStorageCaptureTemplate(name, uri)

                offer(template)
            }
            close()
        }

        awaitClose()
    }

    fun updateFavoriteOfCapture(capture: Capture, shouldBeFavorited: Boolean): Boolean {
        val favoriteCaptureDetails = ContentValues().apply {
            put(ImageMedia.IS_FAVORITE, shouldBeFavorited.toInt())
        }

        return try {
            resolver.update(capture.uri, favoriteCaptureDetails, null, null)
            true
        } catch (e: Throwable) {
            false
        }
    }
}