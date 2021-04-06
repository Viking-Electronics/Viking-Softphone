package com.vikingelectronics.softphone.devices

import android.net.Uri
import android.os.Parcelable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.devices.list.DeviceListViewModel
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Device(
    @DocumentId val id: String = "",
    val name: String = "",
    val callAddress: String = "",
    val allActivityEntries: List<ActivityEntry>? = null
): Parcelable {
    @IgnoredOnParcel
    lateinit var latestActivityEntry: ActivityEntry
    @IgnoredOnParcel
    val activityEntryRefs: List<DocumentReference> = listOf()
}

@Composable
fun DeviceCard(
    device: Device,
    modifier: Modifier = Modifier,
) {
    val viewModel: DeviceListViewModel = hiltNavGraphViewModel()
    Card(
        modifier = modifier
//            .padding(horizontal = 16.dp)
//            .padding(top = 8.dp, bottom = 8.dp)
            .fillMaxWidth(),
        backgroundColor = colorResource(id = R.color.light_grey_color),
        elevation = 4.dp
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Text(text = device.name)
            Box(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                CoilImage(
                    data = device.latestActivityEntry.snapshotUrl,
                    contentDescription = "Latest snapshot from ${device.name}",
                    contentScale = ContentScale.FillWidth,
                    fadeIn = true,
                    loading = {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                Modifier.align(Alignment.Center)
                            )
                        }
                    },
                    error = {
                        Column {
                            Text(
                                modifier = Modifier.fillMaxSize(),
                                text = stringResource(id = R.string.snapshot_loading_error)
                            )
                            Text(text = it.toString())
                        }
                    }
                )
                Text(
                    modifier = Modifier.align(Alignment.BottomStart),
                    text = device.latestActivityEntry.timestamp.toDate().toString(),
                    style = TextStyle(background = colorResource(id = R.color.white))
                )
            }
            Text(text = device.latestActivityEntry.description)
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.goLive(device) }
            ) {
                Text("View live feed")
            }
        }
    }
}
