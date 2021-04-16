package com.vikingelectronics.softphone.accounts

import com.squareup.moshi.Json

data class QrReadResult(
    @Json(name = "url") val domain: String,
    val passes: List<Map<Int, String>>,
    @Json(name = "num") val usernameBase: String
){
    data class QrCreds(val username: String, val password: String, val domain: String)

    fun forEasyConsumption(): List<QrCreds> {
        val credsList = mutableListOf<QrCreds>()
        val pairList = passes.map {
            val entry = it.entries.first()
            Pair(entry.key, entry.value)
        }
        for (entry in pairList) {
            val key = entry.first
            val username = "${usernameBase}u$key"
            val password = entry.second ?: ""
            credsList.add(QrCreds(username, password, domain))
        }
        return credsList
    }
}
