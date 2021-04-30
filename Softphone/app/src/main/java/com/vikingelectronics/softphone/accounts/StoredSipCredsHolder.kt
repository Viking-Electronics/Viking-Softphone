package com.vikingelectronics.softphone.accounts


data class StoredSipCredsHolder(
    val accountBase: String,
    val domain: String,
    val username: String,
    val password: String,
    val displayName: String? = null
)
