package com.vikingelectronics.softphone.accounts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.squareup.moshi.Moshi
import com.tfcporciuncula.flow.FlowSharedPreferences
import com.tfcporciuncula.flow.NullableSerializer
import com.vikingelectronics.softphone.dagger.UserComponent
import com.vikingelectronics.softphone.dagger.UserComponentEntryPoint
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.extensions.nonSettable
import com.vikingelectronics.softphone.extensions.timber
import dagger.hilt.EntryPoints
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.linphone.core.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class UserProvider @OptIn(ExperimentalCoroutinesApi::class)
@Inject constructor(
    preferences: FlowSharedPreferences,
    moshi: Moshi,
    core: Core,
    private val userComponentProvider: Provider<UserComponent.Builder>,
    private val repository: LoginRepository
) {

    private val registrationStateListener = object : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState?,
            message: String
        ) {
            super.onAccountRegistrationStateChanged(core, account, state, message)
            sipRegistrationStatus = state ?: RegistrationState.None
            message.timber()
        }
    }

    private val credsAdapter = moshi.adapter(StoredSipCredsHolder::class.java)
    private val storedCredsSerializer = object: NullableSerializer<StoredSipCredsHolder> {
        override fun deserialize(serialized: String?): StoredSipCredsHolder? = credsAdapter.fromJson(serialized)
        override fun serialize(value: StoredSipCredsHolder?): String? = credsAdapter.toJson(value)
    }


    private val _isLoggedIn = preferences.getBoolean("isLoggedIn", false)
    val isLoggedIn = _isLoggedIn.nonSettable()
    private val _storedSipCreds = preferences.getNullableObject("stored_sip_creds", storedCredsSerializer, null)
    val storedSipCreds = _storedSipCreds.nonSettable()

    var sipRegistrationStatus by mutableStateOf(RegistrationState.None)
        private set

    private var userComponent: UserComponent? = null
    val userComponentEntryPoint: UserComponentEntryPoint
        get() = EntryPoints.get(userComponent, UserComponentEntryPoint::class.java)


    init {
        core.addListener(registrationStateListener)
    }


    suspend fun checkStoredSipCreds() {
        _isLoggedIn.set(_storedSipCreds.isSet())

        val creds = _storedSipCreds.get()
        if (_isLoggedIn.get() && creds != null) userAuthenticatedSuccessfully(creds)

    }

    suspend fun userAuthenticatedSuccessfully(holder: StoredSipCredsHolder): Boolean {
        val userRef = repository.attemptUserFetch(holder.username)
            ?: repository.createUserAccount(holder.username)
        val user = repository.getAwaitObject<User>(userRef) ?: return false

        val sipRef = repository.attemptSipFetch(holder.accountBase)
            ?: repository.createSipAccount(userRef, holder.accountBase)
        val sipAccount: SipAccount =  repository.getAwaitObject<SipAccount>(sipRef) ?: return false

        if (!user.sipAccountExists()) repository.associateSipAccount(user, userRef, sipRef)

        _storedSipCreds.set(holder)
        _isLoggedIn.set(true)

        userComponent = userComponentProvider.get().setUser(user).setSip(sipAccount).build()

        return true
    }

    fun rebindSipAccountWithDevicesIfNecessary(user: User, sipAccount: SipAccount, devices: List<Device>) {
        if (sipAccount.deviceObjects.containsAll(devices)) return

        val newSip = sipAccount.copy(deviceObjects = devices)

        userComponent = userComponentProvider.get().setUser(user).setSip(newSip).build()
    }
}