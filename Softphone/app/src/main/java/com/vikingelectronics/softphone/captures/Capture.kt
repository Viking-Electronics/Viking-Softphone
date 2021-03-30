package com.vikingelectronics.softphone.captures

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.StorageReference
import com.mikepenz.iconics.compose.ExperimentalIconics
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.captures.list.CapturesListViewModel
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.datetime.Clock
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Capture (
    val name: String,
    val uri: Uri,
    val creationTimeMillis: Long,
    val sizeInBytes: Long,
    val type: String,
    val isStoredLocally: Boolean = false
): Parcelable {
    @IgnoredOnParcel
    var isFavorite: Boolean by mutableStateOf(false)
    @IgnoredOnParcel
    lateinit var storageReference: StorageReference
    @IgnoredOnParcel
    var sourceRef: DocumentReference? = null

    fun sizeConverted(context: Context): String = Formatter.formatFileSize(context, sizeInBytes)

    val timeConverted: String by lazy {
        val now: Long = Clock.System.now().toEpochMilliseconds()

         "Captured ${DateUtils.getRelativeTimeSpanString(creationTimeMillis, now, 1000L)}"
    }
}

@OptIn(ExperimentalIconics::class)
@Composable
fun RecordCard (
    capture: Capture,
    navController: NavController
) {

    val viewModel: CapturesListViewModel = viewModel()
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
        Box (
            modifier = Modifier.fillMaxSize(),
        ) {

            Row {

                Box(
                    modifier = Modifier.width(125.dp),
                ) {
                    CoilImage(
                        data = capture.uri,
                        contentDescription = "Record of activity from ${capture.sourceRef}",
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Crop
                    )
                    if (capture.isFavorite) {
                        Icon (
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorite icon",
                            tint = colorResource(id = R.color.red_color),
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )
                    }
                }

                Column (
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = capture.timeConverted
                    )
                    Text(
                        text = capture.sizeConverted(LocalContext.current)
                    )
                    Text(
                        text = capture.sourceRef.toString()
                    )
                }
            }

            Icon(
                imageVector = if (capture.isStoredLocally) Icons.Filled.StayCurrentPortrait  else Icons.Outlined.Cloud,
                contentDescription = "Capture storage location",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 14.dp)
                    .width(14.dp)
                    .height(14.dp))

            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
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
                            viewModel.favoriteRecord(capture)
                            menuExpanded = false
                        }
                    ) {
                        val text = if (capture.isFavorite) "Unfavorite" else "Favorite"
                        Text(text = text)
                    }

                    DropdownMenuItem(
                        onClick = {
                            viewModel.deleteRecord(capture)
                            menuExpanded = false
                        }
                    ) {
                        Text(text = "Delete")
                    }

                    if (!capture.isStoredLocally) {
                        DropdownMenuItem(
                            onClick = {
                                viewModel.downloadRecord(capture)
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
}

@Composable
fun deleteConfirmationDialog(
    capture: Capture
) {
//    AlertDialog(onDismissRequest = { /*TODO*/ }) {
//
//    }
}