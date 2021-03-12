package com.vikingelectronics.softphone.accounts

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference

data class SipAccount(
    @DocumentId val id: String = "",
    val users: List<DocumentReference> = listOf(),
    val devices: List<DocumentReference> = listOf()
)