package com.vikingelectronics.softphone.schedules

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentManager
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.databinding.FragmentContainerBinding
import com.vikingelectronics.softphone.legacy.schedules.ScheduleFragment

@Composable
fun SchedulesScreen(
    supportFragmentManager: FragmentManager
): @Composable RowScope.() -> Unit {


    val toolbarActions: @Composable RowScope.() -> Unit = {
        Switch(checked = false, null)
        Button(onClick = { /*TODO*/ }) {

        }
        Button(onClick = { /*TODO*/ }) {

        }
    }

    AndroidViewBinding(FragmentContainerBinding::inflate) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ScheduleFragment())
            .commit()
    }

    return toolbarActions
}