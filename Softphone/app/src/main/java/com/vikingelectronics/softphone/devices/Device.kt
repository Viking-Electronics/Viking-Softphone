package com.vikingelectronics.softphone.devices

import android.net.Uri
import android.os.Parcelable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
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
import com.google.accompanist.coil.CoilImage
import com.google.accompanist.coil.rememberCoilPainter
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
    var latestActivityEntry: ActivityEntry? = null
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
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .defaultMinSize(minHeight = 100.dp)
            ) {
                device.latestActivityEntry?.let {
                    Image(
                        painter = rememberCoilPainter(
                            request = device.latestActivityEntry?.snapshotUrl
                        ),
                        contentDescription = "Latest snapshot from ${device.name}",
                        contentScale = ContentScale.FillWidth,
                    )
                    Text(
                        modifier = Modifier.align(Alignment.BottomStart),
                        text = device.latestActivityEntry?.timestamp?.toDate().toString(),
                        style = TextStyle(background = colorResource(id = R.color.white))
                    )
                } ?: Image(
                    modifier = Modifier.fillMaxWidth().size(55.dp).align(Alignment.Center),
                    imageVector = Icons.Default.Photo,
                    contentDescription = "Default photo icon"
                )
            }
            Text(text = device.latestActivityEntry?.description ?: "No activity entry available")
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.goLive(device) }
            ) {
                Text("View live feed")
            }
        }
    }
}
