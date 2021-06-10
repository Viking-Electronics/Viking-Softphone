package com.vikingelectronics.softphone.activity

import android.os.Parcelable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.coil.rememberCoilPainter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ActivityEntryCard (
    entry: ActivityEntry,
    selectedState: MutableState<Boolean> = mutableStateOf(false),
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        elevation = 4.dp,
        backgroundColor = if (selectedState.value) Color.Blue else Color.White,
    ) {
        Row (
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = rememberCoilPainter(request = entry.snapshotUrl),
                contentDescription = "Image from activity entry",
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(80.dp)
            )

            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = entry.sourceName)
                Text(text = entry.description)
                Text(text = entry.timestamp.toDate().toString())
            }
        }
    }
}
