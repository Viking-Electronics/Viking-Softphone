package com.vikingelectronics.shared.linphone

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub

actual class Factory internal  constructor(val android: org.linphone.core.Factory) {
    actual fun createAddress(
        address: String
    ): Address? = android.createAddress(address)?.run { Address(this) }

    actual fun createAuthInfo(
        username: String,
        password: String,
        domain: String,
        userId: String?,
        realm: String?,
        ha1: String?
    ): AuthInfo = android.createAuthInfo(username, userId, password, ha1, realm, domain).run { AuthInfo(this) }
}

actual class Address internal constructor(val android: org.linphone.core.Address) {
    actual var transport: TransportType = android.transport
}

actual class AuthInfo internal constructor(val android: org.linphone.core.AuthInfo)

actual class AccountParams internal constructor(val android: org.linphone.core.AccountParams) {
    actual var identityAddress: Address?
        get() = android.identityAddress?.run { Address(this) }
        set(value) { android.identityAddress = value?.android }
    actual var serverAddress: Address?
        get() = android.serverAddress?.run { Address(this) }
        set(value) { android.serverAddress = value?.android }
    actual var registerEnabled: Boolean = android.registerEnabled

}

actual class Core internal constructor(val android: org.linphone.core.Core) {
    actual val currentCall: Call?
        get() = android.currentCall?.run { Call(this) }
    actual val accountList: List<Account>
        get() = android.accountList.map { Account(it) }
    actual var defaultAccount: Account? = android.defaultAccount?.let { Account(it) }
    actual val callState: Flow<State> = callbackFlow {
        val callListener = object: CoreListenerStub() {
            override fun onCallStateChanged(
                core: Core,
                call: org.linphone.core.Call,
                state: org.linphone.core.Call.State?,
                message: String
            ) {
                super.onCallStateChanged(core, call, state, message)
                state?.let { trySend(it) }
            }
        }
        android.addListener(callListener)

        awaitClose { android.removeListener(callListener) }
    }
    actual val accountRegistrationState: Flow<RegistrationState> = callbackFlow {
        val accountRegistrationListener = object : CoreListenerStub() {
            override fun onAccountRegistrationStateChanged(
                core: Core,
                account: org.linphone.core.Account,
                state: org.linphone.core.RegistrationState?,
                message: String
            ) {
                super.onAccountRegistrationStateChanged(core, account, state, message)
                state?.let { trySend(it) }
            }
        }
        android.addListener(accountRegistrationListener)

        awaitClose { android.removeListener(accountRegistrationListener) }
    }

    actual fun createCallParams(call: Call?): CallParams? = android.createCallParams(call?.android)?.run { CallParams(this) }

    actual fun addAuthInfo(authInfo: AuthInfo) = android.addAuthInfo(authInfo.android)

    actual fun createAccountParams(): AccountParams = android.createAccountParams().run { AccountParams(this) }

    actual fun createAccountWithParams(paramsInitializer: AccountParams.() -> Unit): Account {
        val params = createAccountParams().apply(paramsInitializer)

        return android.createAccount(params.android).run { Account(this) }
    }

    actual fun addAccount(account: Account): Boolean = android.addAccount(account.android) == 1

    actual fun removeAccount(account: Account) = android.removeAccount(account.android)

    actual fun inviteAddressWithParams(address: Address, params: CallParams): Call?
        = android.inviteAddressWithParams(address.android, params.android)?.run { Call(this) }

    actual fun startEchoTester(sampleRate: Int): Int = android.startEchoTester(sampleRate)
    actual fun stopEchoTester(): Int = android.stopEchoTester()
    actual fun startEchoCancellerCalibration(): Int = android.startEchoCancellerCalibration()
}

actual class Account internal constructor(val android: org.linphone.core.Account)

actual typealias RegistrationState = org.linphone.core.RegistrationState
actual typealias TransportType = org.linphone.core.TransportType