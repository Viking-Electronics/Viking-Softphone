package com.vikingelectronics.softphone.accounts

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.vikingelectronics.softphone.devices.Device

data class SipAccount(
    @DocumentId val id: String = "",
    var users: List<DocumentReference> = listOf(),
    val devices: List<DocumentReference> = listOf()
) {
    lateinit var deviceObjects: List<Device>
    fun deviceObjectsAreInitialized(): Boolean = ::deviceObjects.isInitialized
}