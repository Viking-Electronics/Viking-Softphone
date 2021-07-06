package com.vikingelectronics.shared.captures

import android.content.Context
import android.net.Uri
import android.text.format.DateUtils
import android.text.format.Formatter
import dev.gitlive.firebase.firestore.DocumentReference
import dev.icerock.moko.parcelize.IgnoredOnParcel
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize

@Parcelize
data class Capture (
    val name: String,
    val uri: Uri,
    val creationTimeMillis: Long,
    val sizeInBytes: Long,
    val type: String,
    var isStoredLocally: Boolean = false,
    var isFavorite: Boolean = false,
    var downloadProgress: Float = 0F
): Parcelable {
//    @IgnoredOnParcel
//    var isStoredLocally: Boolean by mutableStateOf(false)
//    @IgnoredOnParcel
//    var isFavorite: Boolean by mutableStateOf(false)
//    @IgnoredOnParcel
//    var downloadProgress: Float by mutableStateOf(0f)
//
//    @IgnoredOnParcel
//    lateinit var storageReference: StorageReference
    @IgnoredOnParcel
    var sourceRef: DocumentReference? = null

    fun sizeConverted(context: Context): String = Formatter.formatFileSize(context, sizeInBytes)


//    //TODO: convert to string res
//    val timeConverted: String by lazy {
//        val now: Long = Clock.System.now().toEpochMilliseconds()
//
//         "Captured ${DateUtils.getRelativeTimeSpanString(creationTimeMillis, now, 1000L)}"
//    }
}

