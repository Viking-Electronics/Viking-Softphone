package com.vikingelectronics.softphone.schedules.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.extensions.rememberMaterialDialog
import com.vikingelectronics.softphone.schedules.SchedulesViewModel
import com.vikingelectronics.shared.schedules.Schedule

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduleCard(
    modifier: Modifier = Modifier,
    schedule: Schedule,
    backStackEntry: NavBackStackEntry,
    onScheduleEdit: (Schedule) -> Unit,
    onDeleteConfirmed: (Schedule) -> Unit,
) {

    val viewModel: SchedulesViewModel = hiltViewModel(backStackEntry)

    var isActive by remember { mutableStateOf(schedule.enabled) }
    var moreMenuExpanded by remember { mutableStateOf(false) }

    val editDialog = rememberMaterialDialog {
        ScheduleBuilderDialog(schedule, onScheduleEdit)
    }
    val deleteDialog = rememberMaterialDialog {
        ScheduleDeleteConfirmationDialog {
            onDeleteConfirmed(schedule)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp),
        elevation = if (isActive) 4.dp else 0.dp,
        backgroundColor = if (isActive) MaterialTheme.colors.surface else Color.LightGray
    ) {

        Box(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Schedule Icon",
                    tint = if (isActive) Color.Green else Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = schedule.activeDaysToString())
                    Text(text = schedule.timeframeToDisplayString())
                }
            }

            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = isActive,
                    onCheckedChange = {
                        isActive = !isActive
                        viewModel.scheduleEnabledChanged(schedule, isActive)
                    },
                )

                IconButton(
                    onClick = { moreMenuExpanded = true },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Menu")

                    DropdownMenu(
                        expanded = moreMenuExpanded,
                        onDismissRequest = { moreMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                moreMenuExpanded = false
                                deleteDialog.show()
                            }
                        ) {
                            Text(text = stringResource(id = R.string.delete))
                        }
                        
                        DropdownMenuItem(
                            onClick = {
                                moreMenuExpanded = false
                                editDialog.show()
                            }
                        ) {
                            Text(text = stringResource(id = R.string.content_description_edit))
                        }
                    }
                }
            }
        }
    }
}
