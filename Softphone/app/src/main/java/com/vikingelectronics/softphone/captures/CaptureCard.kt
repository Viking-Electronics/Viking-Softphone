package com.vikingelectronics.softphone.captures

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.coil.rememberCoilPainter
import com.vikingelectronics.shared.captures.Capture
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.captures.list.CapturesListViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun CaptureCard (
    capture: Capture,
    navController: NavController,
    selectedState: MutableState<Boolean>,
    onDeleteSuccess: () -> Unit, //TODO: Not thrilled with this if the card ends up being reused
    longClick: (() -> Unit)? = null
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
        onClick = {},
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        backgroundColor = if (selectedState.value) Color.Blue else Color.White
    ) {
        Box (
            modifier = Modifier.fillMaxSize(),
        ) {

            Row {

                Box(
                    modifier = Modifier.width(125.dp),
                ) {
                    {}
                    Image(
                        painter = rememberCoilPainter(
                            request = capture.uri,
                            shouldRefetchOnSizeChange = { _, _ -> false },
                        ),
                        contentDescription = "Record of activity from ${capture.sourceRef}",
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Crop,
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
                        text = "Capture.timeConverted need to do"//capture.timeConverted
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
                    viewModel.deleteCapture(capture, onDeleteSuccess).collect {
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
        onDismissRequest = onDismiss,
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