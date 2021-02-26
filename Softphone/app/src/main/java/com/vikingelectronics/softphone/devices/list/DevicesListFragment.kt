package com.vikingelectronics.softphone.devices.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.devices.Device

class DevicesListFragment {
}


@Preview
@Composable
fun DevicesListScreen() {
    val viewModel: DevicesListViewModel = viewModel()
//    LazyColumn {
        for (device in viewModel.devicesList) {
            DeviceCard(device = device)
        }
//    }
}

@Composable
fun DeviceCard(
    device: Device,
    modifier: Modifier = Modifier,

) {
    val viewModel: DevicesListViewModel = viewModel()
    Card(
        modifier = modifier,
        backgroundColor = colorResource(id = R.color.red_color)
    ) {
        Column() {
            Text(text = device.name)
            Image(bitmap = device.lastActivity.snapshot, contentDescription = "Last image from ${device.name}")
            Text(text = device.lastActivity.timestamp)
            Text(text = device.lastActivity.activityDescription)
            Button(onClick = { viewModel.goLive(device) }) {
                Text("View  live feed")
            }
        }
    }
}