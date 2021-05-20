package com.vikingelectronics.softphone.devices.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.items
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.vikingelectronics.softphone.devices.DeviceCard
import com.vikingelectronics.softphone.extensions.setParcelableAndNavigate
import com.vikingelectronics.softphone.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun DevicesList(
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    val viewModel: DeviceListViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    val lazyDevices = viewModel.devicesList.collectAsLazyPagingItems()
    var isRefreshing by remember { mutableStateOf(false) }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = lazyDevices::refresh,
        modifier = Modifier.fillMaxSize(),
        swipeEnabled = true,
        refreshTriggerDistance = 80.dp,
        indicatorAlignment = Alignment.TopCenter,
        indicatorPadding = PaddingValues(0.dp),
        indicator = { s, trigger -> SwipeRefreshIndicator(s, trigger) }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(lazyDevices) { device ->
                device ?: return@items

                DeviceCard(
                    device = device,
                    navController= navController,
                    modifier = Modifier.clickable {
                        navController.setParcelableAndNavigate(Screen.Secondary.DeviceDetail, device)
                    }
                )
            }
        }

        lazyDevices.apply {
            when (loadState.refresh) {
                is LoadState.Loading -> isRefreshing = true
                is LoadState.NotLoading -> isRefreshing = false
                //TODO: Error gets thrown multiple times here. Probably a lib issue, ref: AND-55
                is LoadState.Error -> {
                    val error = (loadState.refresh as LoadState.Error).error.message ?: "Something went wrong"
                    scope.launch {
                        when(snackbarHostState.showSnackbar("Something went wrong: $error", "Retry")) {
                            SnackbarResult.ActionPerformed -> {
                                isRefreshing = true
                                lazyDevices.retry()
                            }
                            SnackbarResult.Dismissed -> isRefreshing = false
                        }
                    }
                }
            }
        }
    }



}



