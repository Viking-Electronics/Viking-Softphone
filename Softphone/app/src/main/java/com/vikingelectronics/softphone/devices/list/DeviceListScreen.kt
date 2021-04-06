package com.vikingelectronics.softphone.devices.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import com.vikingelectronics.softphone.devices.DeviceCard
import com.vikingelectronics.softphone.extensions.setParcelableAndNavigate
import com.vikingelectronics.softphone.navigation.Screen

@Composable
fun DevicesListScreen(
    navController: NavController,
) {
    val viewModel: DeviceListViewModel = hiltNavGraphViewModel()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(viewModel.devices) { device ->
            DeviceCard(
                device = device,
                modifier = Modifier.clickable {
                    navController.setParcelableAndNavigate(Screen.Secondary.DeviceDetail, device)
                }
            )
        }
    }
}



