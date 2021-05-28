package com.vikingelectronics.softphone.accounts


data class StoredSipCredentials(
    val accountBase: String,
    val domain: String,
    val username: String,
    val password: String,
    val displayName: String? = null
)
