package com.vikingelectronics.softphone.schedules.ui

import androidx.compose.runtime.Composable
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.buttons
import com.vanpra.composematerialdialogs.message
import com.vanpra.composematerialdialogs.title
import com.vikingelectronics.softphone.R


@Composable
fun MaterialDialog.ScheduleDeleteConfirmationDialog(
    onConfirmDelete: () -> Unit,
) {
    title("Delete Schedule")
    message("Are you sure you want to delete this schedule?")

    buttons {
        positiveButton(
            res = R.string.delete ,
            onClick = onConfirmDelete
        )

        negativeButton(res = R.string.cancel)
    }
}