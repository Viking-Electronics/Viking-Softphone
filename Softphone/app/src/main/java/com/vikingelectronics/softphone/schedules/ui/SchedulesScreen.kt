package com.vikingelectronics.softphone.schedules.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.vikingelectronics.softphone.extensions.rememberMaterialDialog
import com.vikingelectronics.softphone.schedules.SchedulesViewModel
import com.vikingelectronics.softphone.schedules.data.Schedule
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun SchedulesScreen(
    backStackEntry: NavBackStackEntry,
    snackbarHostState: SnackbarHostState = rememberScaffoldState().snackbarHostState
): @Composable RowScope.() -> Unit {

    val viewModel: SchedulesViewModel = hiltViewModel(backStackEntry)

    val isSnoozed by viewModel.isGloballySnoozed.collectAsState(false)
    val schedules = viewModel.schedules.collectAsLazyPagingItems()
    val multiSelectedSchedules by remember { mutableStateOf(mutableListOf<Schedule>()) }

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    fun onFailure(throwable: Throwable){
        scope.launch {
            snackbarHostState.showSnackbar("There was a problem with your schedule, please try again")
        }
    }

    fun onSuccess(u: Unit = Unit) { schedules.refresh() }
    fun onScheduleProduced(updatedSchedule: Schedule) {
        isRefreshing = true
        viewModel.updateSchedule(updatedSchedule, ::onSuccess, ::onFailure)
    }
    fun onSingleDeleteConfirmed(scheduleForDelete: Schedule) {
        isRefreshing = true
        viewModel.deleteSchedules(listOf(scheduleForDelete), schedules::refresh, ::onFailure)
    }

    val addDialog = rememberMaterialDialog {
        ScheduleBuilderDialog {
            viewModel.saveSchedule(it, ::onSuccess, ::onFailure)
        }
    }
    val deleteDialog = rememberMaterialDialog {
        ScheduleDeleteConfirmationDialog {
            viewModel.deleteSchedules(multiSelectedSchedules, ::onSuccess, ::onFailure)
        }
    }


    val toolbarActions: @Composable RowScope.() -> Unit = {
        Row(
            Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = "Snooze",
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Switch(
                checked = isSnoozed,
                onCheckedChange = viewModel::globalSnoozeClicked
            )
        }

        AnimatedVisibility(visible = multiSelectedSchedules.isNotEmpty()) {
            IconButton(onClick = deleteDialog::show) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Selected Icon")
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
            onRefresh = schedules::refresh
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(schedules) { schedule ->
                    schedule ?: return@items
                    ScheduleCard(
                        schedule = schedule,
                        backStackEntry = backStackEntry,
                        onScheduleEdit = ::onScheduleProduced,
                        onDeleteConfirmed = ::onSingleDeleteConfirmed,
                    )
                }
            }

            schedules.apply {
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
                                    schedules.retry()
                                }
                                SnackbarResult.Dismissed -> isRefreshing = false
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = addDialog::show,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Schedule Button",
                tint = Color.White
            )
        }
    }


    return toolbarActions
}