package com.vikingelectronics.softphone.records

import android.net.Uri
import android.os.Parcelable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.StorageReference
import com.mikepenz.iconics.compose.ExperimentalIconics
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.records.list.RecordsViewModel
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Record (
    val name: String,
    val imageUri: Uri,
    val timestamp: String,
    val size: String,
): Parcelable {
    @IgnoredOnParcel
    var isFavorite: Boolean by mutableStateOf(false)
    @IgnoredOnParcel
    lateinit var storageReference: StorageReference
    @IgnoredOnParcel
    var sourceRef: DocumentReference? = null
}

@OptIn(ExperimentalIconics::class)
@Composable
fun RecordCard (
    record: Record,
    navController: NavController
) {

    val viewModel: RecordsViewModel = viewModel()
    var menuExpanded by remember { mutableStateOf(false) }

    Card (
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable {
//                val directions =
            }
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row {

                Box(
                    modifier = Modifier.width(125.dp),
                ) {
                    CoilImage(
                        data = record.imageUri,
                        contentDescription = "Record of activity from ${record.sourceRef}",
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Crop
                    )
                    if (record.isFavorite) {
                        Icon (
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorite icon",
                            tint = colorResource(id = R.color.red_color),
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )
                    }
                }

                Column (
                    modifier = Modifier.padding(start = 8.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = record.timestamp
                    )
                    Text(
                        text = record.size
                    )
                    Text(
                        text = record.sourceRef.toString()
                    )
                }
            }

            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(24.dp)
                    .height(24.dp)
            ) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Menu")

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }) {

                    DropdownMenuItem(
                        onClick = {
                            viewModel.favoriteRecord(record)
                            menuExpanded = false
                        }
                    ) {
                        val text = if (record.isFavorite) "Unfavorite" else "Favorite"
                        Text(text = text)
                    }

                    DropdownMenuItem(
                        onClick = {
                            viewModel.deleteRecord(record)
                            menuExpanded = false
                        }
                    ) {
                        Text(text = "Delete")
                    }

                    DropdownMenuItem(
                        onClick = {
                            viewModel.downloadRecord(record)
                            menuExpanded = false
                        }
                    ) {
                        Text(text = "Download")
                    }
                }
            }
        }
    }
}

@Composable
fun deleteConfirmationDialog(
    record: Record
) {
//    AlertDialog(onDismissRequest = { /*TODO*/ }) {
//
//    }
}