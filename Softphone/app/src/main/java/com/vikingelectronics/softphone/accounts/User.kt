package com.vikingelectronics.softphone.accounts

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference

data class User(
    @DocumentId val id: String = "",
    val pushToken: String = "",
    val username: String = "",
) {
    lateinit var sipAccount: DocumentReference
    fun sipAccountExists() = ::sipAccount.isInitialized
}
