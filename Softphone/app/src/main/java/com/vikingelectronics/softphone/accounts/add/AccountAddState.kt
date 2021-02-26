package com.vikingelectronics.softphone.accounts.add

import androidx.lifecycle.LiveData
import com.etiennelenhart.eiffel.binding.BindableProperty1
import com.etiennelenhart.eiffel.binding.BindableState
import com.etiennelenhart.eiffel.state.State

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
): State {
    sealed class TransportType {
        data class UDP(val name: String = "UDP"): TransportType()
        data class TCP(val name: String = "TCP"): TransportType()
        data class TLS(val name: String = "TLS"): TransportType()
    }
}

class AccountAddBindableState(state: LiveData<AccountAddState>): BindableState<AccountAddState>(state) {
    val usernameError by bindableProperty(false) { it.usernameError }
}