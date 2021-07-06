package com.vikingelectronics.shared.accounts

import com.vikingelectronics.shared.devices.Device
import com.vikingelectronics.shared.schedules.Schedule
import dev.gitlive.firebase.firestore.DocumentReference

data class SipAccount(
    val id: String = "",
    var users: List<DocumentReference> = listOf(),
    val devices: List<DocumentReference> = listOf(),
    val deviceObjects: List<Device> = listOf(),
    val schedules: List<Schedule> = listOf()
)