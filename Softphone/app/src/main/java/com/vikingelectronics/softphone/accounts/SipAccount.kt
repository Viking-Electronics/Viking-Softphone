package com.vikingelectronics.softphone.accounts

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.schedules.data.Schedule

data class SipAccount(
    @DocumentId val id: String = "",
    var users: List<DocumentReference> = listOf(),
    val devices: List<DocumentReference> = listOf(),
    val deviceObjects: List<Device> = listOf(),
    val schedules: List<Schedule> = listOf()
)