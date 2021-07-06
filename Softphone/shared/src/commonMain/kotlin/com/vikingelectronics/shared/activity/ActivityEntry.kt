package com.vikingelectronics.shared.activity


import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.FieldValue
import dev.icerock.moko.parcelize.IgnoredOnParcel
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ActivityEntry(
    val id: String = "",
    val timestamp: Double = FieldValue.serverTimestamp,
    val snapshotUrl:  String = "",
    val description: String = "",
    val sourceName: String = ""
) {
    @Contextual
    var sourceDevice: DocumentReference? = null
}
