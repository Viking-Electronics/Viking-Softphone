package com.vikingelectronics.softphone.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.devices.Device

interface NavigationDestination {
     val labelId: Int
     val iconId: Int
}

sealed class Screen(open val route: String) {
    sealed class TopLevel(override val route: String, @StringRes val toolbarResourceId: Int, val icon: @Composable () -> Unit): Screen(route) {
        object ActivityList: TopLevel("activityList", R.string.activity, {
            Icon(imageVector = Icons.Filled.Timeline, contentDescription = "Activity list icon")
        })
        object DeviceList: TopLevel("deviceList", R.string.devices, {
            Icon(painter = painterResource(id = R.drawable.x35_icon), contentDescription = "Device list icon")
        })
        object CaptureList: TopLevel("captureList", R.string.captures, {
            Icon(imageVector = Icons.Filled.PhotoCamera, contentDescription = "Captures list icon")
        })
    }
//    class ActivityDetail(val activity: ActivityEntry): Screen("activityDetail", )

//    class DeviceDetail(val device: Device): Screen("deviceDetail")


//    class CaptureDetail(val capture: Capture): Screen("captureDetail",)
}
