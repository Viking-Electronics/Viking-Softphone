package com.vikingelectronics.shared.accounts

import dev.gitlive.firebase.firestore.DocumentReference


data class User(
    val id: String = "",
    val pushToken: String = "",
    val username: String = "",
) {
    lateinit var sipAccount: DocumentReference
    fun sipAccountExists() = ::sipAccount.isInitialized
}
