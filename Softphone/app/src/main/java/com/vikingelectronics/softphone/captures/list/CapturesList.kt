package com.vikingelectronics.softphone.captures.list


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
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
import com.vikingelectronics.shared.captures.Capture
import com.vikingelectronics.softphone.captures.CaptureCard
import kotlinx.coroutines.launch

@Composable
fun CapturesList(
    navController: NavController,
    shouldShowToolbarActions: MutableState<Boolean>,
    snackbarHostState: SnackbarHostState = rememberScaffoldState().snackbarHostState
): @Composable RowScope.() -> Unit {

    val viewModel: CapturesListViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    val lazyCaptures = viewModel.capturesList.collectAsLazyPagingItems()
    var isRefreshing by remember { mutableStateOf(false) }
    val selectedCaptures by remember { mutableStateOf(mutableListOf<Capture>()) }
    val toolbarActions: @Composable RowScope.() -> Unit = {
        Button(onClick = {
            selectedCaptures.forEach { viewModel }
        }) {
            Icon(imageVector = Icons.Default.Download, contentDescription = "Download Icon")
        }
        Button(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
        }
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = lazyCaptures::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn (
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(lazyCaptures) { capture ->
                if (capture == null) return@items
                val selectedState = mutableStateOf(false)

                CaptureCard(capture = capture, navController, selectedState, lazyCaptures::refresh) {
//                    if (selectedCaptures.contains(capture)) {
//                        selectedCaptures.remove(capture)
//                        selectedState.value = false
//                    } else {
//                        selectedCaptures.add(capture)
//                        selectedState.value = true
//                    }
//                    shouldShowToolbarActions.value = selectedCaptures.isNotEmpty()
                }
            }

            lazyCaptures.apply {
                when(loadState.refresh){
                    is LoadState.Loading -> isRefreshing = true
                    is LoadState.NotLoading -> isRefreshing = false
                    is LoadState.Error -> {
                        val error = (loadState.refresh as LoadState.Error).error.message ?: "Something went wrong"
                        scope.launch {
                            when(snackbarHostState.showSnackbar("Something went wrong: $error", "Retry")) {
                                SnackbarResult.ActionPerformed -> {
                                    isRefreshing = true
                                    lazyCaptures.retry()
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