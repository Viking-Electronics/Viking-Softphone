package com.vikingelectronics.softphone.storage

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.firebase.storage.FirebaseStorage
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.extensions.timber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject


typealias ImageMedia = MediaStore.Images.Media

class LocalCaptureDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    sealed class DownloadState {
        class Success(val captureUri: Uri): DownloadState()
        class Downloading(val progress: Int): DownloadState()
        class Failure(val error: Throwable): DownloadState()
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
        ImageMedia.DATE_ADDED,
        ImageMedia.SIZE,
        ImageMedia.DISPLAY_NAME
    )
    private val imageQuerySortOrder = "${ImageMedia.DATE_ADDED} DESC"


    //TODO: Figure out why this doesn't work on any other api than 30
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun saveCapture(capture: Capture): Flow<DownloadState> = callbackFlow {

        val newCaptureDetails = ContentValues().apply {
//            put(ImageMedia.DOCUMENT_ID, capture.id)
            put(ImageMedia.DISPLAY_NAME, capture.name)
            put(ImageMedia.MIME_TYPE, capture.type)
            put(ImageMedia.IS_FAVORITE, capture.isFavorite)
            put(ImageMedia.SIZE, capture.sizeInBytes)
            put(ImageMedia.DATE_ADDED, capture.creationTimeMillis)
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
            val percentage = (100f * it.bytesTransferred / it.totalByteCount).toInt().timber("Transferred percentage:")
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
            val state = it.exception?.let { exception -> DownloadState.Failure(exception) } ?: DownloadState.Success(captureUri)
            offer(state)
            channel.close()
        }

        awaitClose()
    }

    suspend fun fetchCapturesFromStorage(): Flow<Capture> = callbackFlow {
        resolver.query(
            imagesCollection,
            imageQueryProjection,
            null,
            null,
            imageQuerySortOrder)?.use { cursor ->

            val idColumn = cursor.getColumnIndex(ImageMedia._ID)
            val nameColumn = cursor.getColumnIndex(ImageMedia.DISPLAY_NAME)
            val favoriteColumn = cursor.getColumnIndex(ImageMedia.IS_FAVORITE)
            val sizeColumn = cursor.getColumnIndex(ImageMedia.SIZE)
            val dateAddedColumn = cursor.getColumnIndex(ImageMedia.DATE_ADDED)
            val mimeTypeColumn = cursor.getColumnIndex(ImageMedia.MIME_TYPE)

//            cursor.moveToFirst()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn).timber()
                val name = cursor.getString(nameColumn)
                val isFavorite = cursor.getInt(favoriteColumn)
                val size = cursor.getLong(sizeColumn)
                val date = cursor.getLong(dateAddedColumn)
                val mime = cursor.getString(mimeTypeColumn)

                val uri = ContentUris.withAppendedId(imagesCollection, id)

                val cap = Capture(name, uri, date, size, mime).apply {
                    this.isFavorite = isFavorite == 1
                }

                offer(cap)
            }
            close()
        }

        awaitClose()
    }
}