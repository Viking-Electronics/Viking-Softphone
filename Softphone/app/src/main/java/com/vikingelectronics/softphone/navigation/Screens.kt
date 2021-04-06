package com.vikingelectronics.softphone.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.devices.Device


sealed class Screen(
    open val route: String,
) {
    sealed class Primary(
        override val route: String,
        @StringRes val toolbarResourceId: Int,
        val icon: @Composable () -> Unit
    ): Screen(route) {

        object ActivityList: Primary("activityList", R.string.activity, {
            Icon(imageVector = Icons.Filled.Timeline, contentDescription = "Activity list icon")
        })
        object DeviceList: Primary("deviceList", R.string.devices, {
            Icon(
                painter = painterResource(id = R.drawable.x35_icon),
                contentDescription = "Device list icon",
                modifier = Modifier.size(22.dp)
            )
        })
        object CaptureList: Primary("captureList", R.string.captures, {
            Icon(imageVector = Icons.Filled.PhotoCamera, contentDescription = "Captures list icon")
        })

        object Schedules: Primary("schedulesList", R.string.schedules, {
            Icon(imageVector = Icons.Filled.Schedule, contentDescription = "Schedules list menu icon")
        })
        object Info: Primary("info", R.string.info, {
            Icon(imageVector = Icons.Filled.Info, contentDescription = "Info menu icon")
        })
        object Settings: Primary("settings", R.string.settings, {
            Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings menu icon")
        })
    }

    sealed class Secondary(
        override val route: String,
        val parcelableKey: String,
    ): Screen(route) {
        object ActivityDetail: Secondary("activityDetail", "activityEntry")
        object DeviceDetail: Secondary("deviceDetail", "device")
        object CaptureDetail: Secondary("captureDetail", "capture")
    }
}
