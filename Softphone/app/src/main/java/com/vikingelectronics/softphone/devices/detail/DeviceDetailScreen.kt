package com.vikingelectronics.softphone.devices.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.activity.ActivityEntryCard
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.extensions.setParcelableAndNavigate
import com.vikingelectronics.softphone.navigation.Screen
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun DeviceDetailScreen(
    device: Device,
    navController: NavController
) {
    val viewModel: DeviceDetailViewModel = hiltNavGraphViewModel()
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

                CoilImage(
                    modifier = Modifier.fillMaxWidth(),
                    data = device.latestActivityEntry.snapshotUrl,
                    contentDescription = "Latest snapshot from ${device.name}",
                    contentScale = ContentScale.Inside,
                )

                Text(
                    text = device.latestActivityEntry.description,
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = device.latestActivityEntry.timestamp.toDate().toString(),
                    style = MaterialTheme.typography.body1
                )

                val activityText = if (viewModel.activityList.isNotEmpty()) "Previous Activity" else "No Previous Activity"

                Text(
                    text = activityText,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

        }

        items(viewModel.activityList) { entry ->
            ActivityEntryCard(
                entry = entry,
                modifier = Modifier.clickable {
                    navController.setParcelableAndNavigate(Screen.Secondary.ActivityDetail, entry)
                }
            )
        }
    }
}