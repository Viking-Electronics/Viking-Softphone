package com.vikingelectronics.softphone.devices

import com.vikingelectronics.softphone.activity.ActivityEntry

data class Device(
    val name: String,
    val lastActivity: ActivityEntry,
    val locationTag: String,
    val callNumber: String,
)
