package com.vikingelectronics.softphone.activity.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.activity.ActivityEntryCard
import com.vikingelectronics.softphone.extensions.setParcelableAndNavigate
import com.vikingelectronics.softphone.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityList(
    navController: NavController,
    shouldShowToolbarActions: MutableState<Boolean>,
    snackbarHostState: SnackbarHostState = rememberScaffoldState().snackbarHostState
): @Composable (RowScope.() -> Unit) {

    val selectedEntries by remember { mutableStateOf(mutableListOf<ActivityEntry>()) }

    val viewModel: ActivityListViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val lazyActivityEntries = viewModel.activityEntries.collectAsLazyPagingItems()
    var isRefreshing by remember { mutableStateOf(false) }
    val toolbarActions: @Composable RowScope.() -> Unit = {
        Button(onClick = {
//            selectedEntries.forEach { viewModel }
        }) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite Icon"
            )
        }

    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = lazyActivityEntries::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(lazyActivityEntries) { item: ActivityEntry? ->
                if (item == null) return@items
                val selectedState = mutableStateOf(false)

                ActivityEntryCard(entry = item, selectedState) {
                    navController.setParcelableAndNavigate(Screen.Secondary.ActivityDetail, item)

                    //Commenting out to preserve multiselect impl until we get around to it.
//                    if (selectedEntries.contains(item)) {
//                        selectedEntries.remove(item)
//                        selectedState.value = false
//                    } else {
//                        selectedEntries.add(item)
//                        selectedState.value = true
//                    }
//                    shouldShowToolbarActions.value = selectedEntries.isNotEmpty()
                }
            }

            lazyActivityEntries.apply {
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
                                    lazyActivityEntries.retry()
                                }
                                SnackbarResult.Dismissed -> isRefreshing = false
                            }
                        }
                    }
                }
            }
        }
    }


    return toolbarActions
}