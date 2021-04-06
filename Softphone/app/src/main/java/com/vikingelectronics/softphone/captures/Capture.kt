package com.vikingelectronics.softphone.captures

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.text.format.DateUtils
import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.StorageReference
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.captures.list.CapturesListViewModel
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
): Parcelable {
    @IgnoredOnParcel
    var isStoredLocally: Boolean by mutableStateOf(false)
    @IgnoredOnParcel
    var isFavorite: Boolean by mutableStateOf(false)
    @IgnoredOnParcel
    var downloadProgress: Float by mutableStateOf(0f)

    @IgnoredOnParcel
    lateinit var storageReference: StorageReference
    @IgnoredOnParcel
    var sourceRef: DocumentReference? = null

    fun sizeConverted(context: Context): String = Formatter.formatFileSize(context, sizeInBytes)


    //TODO: convert to string res
    val timeConverted: String by lazy {
        val now: Long = Clock.System.now().toEpochMilliseconds()

         "Captured ${DateUtils.getRelativeTimeSpanString(creationTimeMillis, now, 1000L)}"
    }
}

@Composable
fun RecordCard (
    capture: Capture,
    navController: NavController,
) {

    val viewModel: CapturesListViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var deleteDialogShowing by remember { mutableStateOf(false) }
    var shouldShowDownloadProgress by remember { mutableStateOf(false) }
    val animatedDownloadProgress = animateFloatAsState(
        targetValue = capture.downloadProgress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    ).value

    Card (
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(enabled = !shouldShowDownloadProgress) {
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
                    if (shouldShowDownloadProgress) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .height(125.dp)
                                .width(125.dp)
                                .background(colorResource(id = R.color.transparent_light_grey))
                        ) {
                            CircularProgressIndicator(
                                progress = animatedDownloadProgress,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

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
                            scope.launch {
                                viewModel.favoriteCapture(capture).collect {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                }
                            }
                            menuExpanded = false
                        }
                    ) {
                        val stringRes = if (capture.isFavorite) R.string.unfavorite else R.string.favorite
                        Text(text = stringResource(id = stringRes))
                    }

                    DropdownMenuItem(
                        onClick = {
                            deleteDialogShowing = true
                            menuExpanded = false
                        }
                    ) {
                        Text(text = stringResource(id = R.string.delete))
                    }

                    if (!capture.isStoredLocally) {
                        DropdownMenuItem(
                            onClick = {
                                shouldShowDownloadProgress = true
                                scope.launch {
                                    viewModel.downloadCapture(capture).collect {
                                        shouldShowDownloadProgress = false
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                menuExpanded = false
                            }
                        ) {
                            Text(text = stringResource(id = R.string.download))
                        }
                    }
                }
            }
        }
    }

    if (deleteDialogShowing) {
        DeleteConfirmationDialog(
            onConfirm = {
                scope.launch {
                    viewModel.deleteCapture(capture).collect {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { deleteDialogShowing = false }
        )
    }

}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(text = stringResource(R.string.cap_delete_dialog_title))
        },
        text = {
           Text(text = stringResource(R.string.cap_delete_dialog_text))
        },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(text = stringResource(R.string.yes))
            }

        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = stringResource(R.string.no))
            }
        }
    )
}