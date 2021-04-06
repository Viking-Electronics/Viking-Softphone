package com.vikingelectronics.softphone.devices.list

import android.os.Bundle
import android.os.Parcelable
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
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import androidx.navigation.findNavController
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.devices.DeviceCard
import com.vikingelectronics.softphone.extensions.setParcelableAndNavigate
import com.vikingelectronics.softphone.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DeviceListFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_generic_compose, container, false).apply {
            findViewById<ComposeView>(R.id.composeView).setContent {
                MaterialTheme {
//                    DevicesListScreen(findNavController())
                }
            }
        }
    }
}


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



