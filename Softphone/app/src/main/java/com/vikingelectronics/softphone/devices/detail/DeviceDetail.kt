package com.vikingelectronics.softphone.devices.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.activity.ActivityEntryCard
import com.vikingelectronics.shared.devices.Device
import com.google.accompanist.coil.rememberCoilPainter
import com.vikingelectronics.softphone.extensions.setParcelableAndNavigate
import com.vikingelectronics.softphone.navigation.Screen

@Composable
fun DeviceDetail(
    device: Device,
    navController: NavController
) {
    val viewModel: DeviceDetailViewModel = hiltViewModel()
    viewModel.getActivityFeedForDevice(device)
    LazyColumn (
        modifier = Modifier
            .background(color = colorResource(id = R.color.light_grey_color))
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {

        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "Latest Activity:",
                    style = MaterialTheme.typography.h5
                )

                device.latestActivityEntry?.let {
                    Image(
                        modifier = Modifier.fillMaxWidth(),
                        painter = rememberCoilPainter(
                            request = it.snapshotUrl
                        ),
                        contentDescription ="Latest snapshot from ${device.name}",
                        contentScale = ContentScale.Inside,
                    )

                    Text(
                        text = device.latestActivityEntry?.description ?: stringResource(R.string.no_activity),
                        style = MaterialTheme.typography.body1
                    )
                    Text(
                        text = device.latestActivityEntry?.timestamp?.toDate().toString(),
                        style = MaterialTheme.typography.body1
                    )

                } ?: Image(
                    modifier = Modifier.fillMaxWidth().size(55.dp),
                    imageVector = Icons.Default.Photo,
                    contentDescription = "Default activity entry icon"
                )

                Row {
                    Text(
                        text = stringResource(id = viewModel.activityText),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(top = 16.dp, end = 16.dp)
                    )

                    if (viewModel.activityText == R.string.loading_previous_activity) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Bottom)
                        )
                    }
                }


            }

        }

        items(viewModel.activityList) { entry ->
            ActivityEntryCard(
                entry = entry,
            ) {
                navController.setParcelableAndNavigate(Screen.Secondary.ActivityDetail, entry)
            }
        }
    }
}