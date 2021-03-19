package com.vikingelectronics.softphone.devices.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.devices.DeviceCard
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DeviceListFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_generic_compose, container, false).apply {
            findViewById<ComposeView>(R.id.composeView).setContent {
                MaterialTheme {
                    DevicesListScreen(findNavController())
                }
            }
        }
    }
}


@Composable
fun DevicesListScreen(navController: NavController) {
    val viewModel: DeviceListViewModel = viewModel()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(viewModel.devices) { device ->
            DeviceCard(
                device = device,
                modifier = Modifier.clickable {
                    val action = DeviceListFragmentDirections.actionDevicesListFragmentToDeviceDetailFragment(device.name, device)
                    navController.navigate(action)
                }
            )
        }
    }
}

