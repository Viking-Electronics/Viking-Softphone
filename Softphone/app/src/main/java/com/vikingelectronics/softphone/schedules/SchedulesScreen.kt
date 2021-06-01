package com.vikingelectronics.softphone.schedules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.dpro.widgets.WeekdaysPicker
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.customView
import com.vanpra.composematerialdialogs.datetime.timepicker.timepicker
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.databinding.FragmentContainerBinding
import com.vikingelectronics.softphone.legacy.schedules.ScheduleFragment

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SchedulesScreen(
    supportFragmentManager: FragmentManager
): @Composable RowScope.() -> Unit {

    val viewModel: SchedulesViewModel = hiltViewModel()
    val addDialog = remember { MaterialDialog() }.apply {
        build {
            customView {
                AndroidView({ context ->
                    WeekdaysPicker(context).apply {
                        setOnWeekdaysChangeListener(viewModel)
                        setEditable(true)
                        showWeekend  = true
//                        this.
                    }
                },
                modifier = Modifier.padding(top = 16.dp))
            }
            timepicker()
        }
    }

    val isSnoozed by viewModel.isGloballySnoozed.collectAsState(false)
    val isInMultiselect by viewModel.isInMultiselect.collectAsState(false)

    val toolbarActions: @Composable RowScope.() -> Unit = {
        Row(
            Modifier.padding(end = 8.dp)
        ) {
            Text(text = "Snooze")
            Switch(checked = isSnoozed, viewModel::globalSnoozeClicked)
        }

        AnimatedVisibility(visible = isInMultiselect) {
            Button(onClick = viewModel::deleteSelectedSchedules) {

            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidViewBinding(
            FragmentContainerBinding::inflate,
            modifier = Modifier.fillMaxSize()
        ) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ScheduleFragment())
                .commit()
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