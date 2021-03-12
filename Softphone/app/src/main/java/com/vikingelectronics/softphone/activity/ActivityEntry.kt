package com.vikingelectronics.softphone.activity

import android.os.Parcelable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ActivityEntry(
    @DocumentId val id: String = "",
    val timestamp: Timestamp = Timestamp(0,0),
    val snapshotUrl:  String = "",
    val description: String = "",
    val sourceName: String = ""
): Parcelable {
    @IgnoredOnParcel
    var sourceDevice: DocumentReference? = null
}

@Composable
fun ActivityEntryCard (
    entry: ActivityEntry,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = 4.dp,
        modifier = modifier
    ) {
        Row (
            modifier = Modifier.fillMaxWidth()
        ) {
            CoilImage(
                data = entry.snapshotUrl,
                contentDescription = "Image from activity entry",
                modifier = Modifier.size(80.dp)
            )

            Column(
                modifier = Modifier.padding(8.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = entry.sourceName)
                Text(text = entry.description)
                Text(text = entry.timestamp.toDate().toString())
            }
        }
    }
}
