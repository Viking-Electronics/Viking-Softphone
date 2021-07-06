package com.vikingelectronics.shared.linphone

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

expect class Factory {
    fun createAddress(address: String): Address?
    fun createAuthInfo(
        username: String,
        password: String,
        domain: String,
        userId: String? = null,
        realm: String? = null,
        ha1: String? = null
    ): AuthInfo
}

expect class Core {
    val currentCall: Call?
    val accountList: List<Account>
    var defaultAccount: Account?

    val callState: Flow<State>
    val accountRegistrationState: Flow<RegistrationState>

    fun createCallParams(call: Call?): CallParams?
    fun addAuthInfo(authInfo: AuthInfo)

    fun createAccountParams(): AccountParams
    fun createAccountWithParams(paramsInitializer: AccountParams.() -> Unit): Account

    fun addAccount(account: Account): Boolean
    fun removeAccount(account: Account)

    fun inviteAddressWithParams(address: Address, params: CallParams): Call?

    fun startEchoTester(sampleRate: Int): Int
    fun stopEchoTester(): Int
    fun startEchoCancellerCalibration(): Int
}

expect class Address {
    var transport: TransportType
}

expect class AuthInfo

expect class Account

expect class AccountParams {
    var identityAddress: Address?
    var serverAddress: Address?
    var registerEnabled: Boolean
}

expect enum class RegistrationState {
    None,
    Progress,
    Ok,
    Cleared,
    Failed;
}

expect enum class TransportType {
    Udp,
    Tcp,
    Tls,
    Dtls;
}