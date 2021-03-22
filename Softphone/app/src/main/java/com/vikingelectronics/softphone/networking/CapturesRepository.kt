package com.vikingelectronics.softphone.networking

import android.content.Context
import android.text.format.DateUtils
import android.text.format.Formatter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import com.vikingelectronics.softphone.captures.Capture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.*
import javax.inject.Inject
import kotlin.Result

interface CapturesRepository {
    suspend fun getExternalCaptures(): Flow<Capture>
    suspend fun updateFavorite(storageReference: StorageReference, shouldBeFavorite: Boolean): Flow<Result<Boolean>>
}

class CapturesRepositoryImpl @Inject constructor(
    override val db: FirebaseFirestore,
    override val storage: FirebaseStorage,
    @ApplicationContext val context: Context,
): FirebaseRepository(), CapturesRepository {


    override suspend fun getExternalCaptures(): Flow<Capture> = flow {
        initStorageRecord()
        storageRef?.listAll()
                ?.await()
                ?.items
//                ?.sortBy { it.metadata.await().creationTimeMillis }
                ?.forEach { emit(generateRecordFromStorageRef(it)) }
    }

    override suspend fun updateFavorite(storageReference: StorageReference, shouldBeFavorite: Boolean): Flow<Result<Boolean>> {
        val metadata = storageMetadata {
            setCustomMetadata(FAVORITE_KEY, shouldBeFavorite.toString())
        }

        return storageReference.updateMetadata(metadata).emitResult()
    }

    private suspend fun generateRecordFromStorageRef(reference: StorageReference): Capture {
        val metadata = reference.metadata.await()
        val downloadUrl = reference.downloadUrl.await()

        val favorite = metadata.getCustomMetadata(FAVORITE_KEY).toBoolean()

        val size = metadata.sizeBytes
        val convertedSize = Formatter.formatFileSize(context, size)
        val timestamp = convertTimestamp(metadata)

        val name = reference.name

        return Capture(name, downloadUrl, timestamp,  convertedSize).apply {
            storageReference = reference
            isFavorite = favorite
        }
    }


    fun getStoredRecords() {

    }

    private fun convertTimestamp(metadata: StorageMetadata): String {

        val time = metadata.creationTimeMillis
        val now: Long = Clock.System.now().toEpochMilliseconds()

        return "Captured ${DateUtils.getRelativeTimeSpanString(time, now, 1000L)}"
//        Timber.d(newTime.toString())
//        if (time > now || time <= 0) {
//            return "Something went wrong with time conversion"
//        }
//
//        // TODO: localize
//        val diff = now - time
//        return when {
//            diff < MINUTE_MILLIS -> "just now"
//            diff < 2 * MINUTE_MILLIS -> "a minute ago"
//            diff < 50 * MINUTE_MILLIS -> "${diff / MINUTE_MILLIS} minutes ago"
//            diff < 90 * MINUTE_MILLIS -> "an hour ago"
//            diff < 24 * HOUR_MILLIS -> "${diff / HOUR_MILLIS} hours ago"
//            diff < 48 * HOUR_MILLIS -> "yesterday"
//            else -> "${diff / DAY_MILLIS} days ago"
//        }
    }
}