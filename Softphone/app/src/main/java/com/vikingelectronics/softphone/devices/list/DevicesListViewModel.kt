package com.vikingelectronics.softphone.devices.list

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.devices.Device

class DevicesListViewModel: ViewModel() {

    private val testDevice = Device(
        "Test",
        ActivityEntry(
            "01:11:11",
            ImageBitmap(20, 20),
            "Someone broke in"
        ),
        "FrontDoor",
        "8675309"
    )
    val devicesList: List<Device> = mutableListOf(testDevice)

    fun goLive(device: Device) {

    }
}