package com.vikingelectronics.softphone.accounts.add


data class AccountAddState(
    val username: String = "",
    val usernameError: Boolean = false,
    val userId: String = "",
    val password: String = "",
    val passwordError: Boolean = false,
    val domain: String = "",
    val domainError: Boolean = false,
    val displayName: String = "",
    val transportType: TransportType = TransportType.UDP(),
) {
    sealed class TransportType {
        data class UDP(val name: String = "UDP"): TransportType()
        data class TCP(val name: String = "TCP"): TransportType()
        data class TLS(val name: String = "TLS"): TransportType()
    }
}