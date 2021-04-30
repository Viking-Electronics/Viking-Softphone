package com.vikingelectronics.softphone.accounts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import com.tfcporciuncula.flow.FlowSharedPreferences
import com.tfcporciuncula.flow.NullableSerializer
import com.vikingelectronics.softphone.dagger.UserComponent
import com.vikingelectronics.softphone.extensions.nonSettable
import com.vikingelectronics.softphone.extensions.timber
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.ProxyConfig
import org.linphone.core.RegistrationState
import org.w3c.dom.Document
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class UserProvider @Inject constructor(
    preferences: FlowSharedPreferences,
    moshi: Moshi,
    core: Core,
    private val repository: LoginRepository
) {

    private val registrationStateListener = object : CoreListenerStub() {
        override fun onRegistrationStateChanged(
            core: Core,
            proxyConfig: ProxyConfig,
            state: RegistrationState?,
            message: String
        ) {
            super.onRegistrationStateChanged(core, proxyConfig, state, message)
            sipRegistrationStatus = state ?: RegistrationState.None
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

    var userComponent: UserComponent? = null
        private set

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
        userRef.get().await().timber()
        val user = repository.getAwaitObject<User>(userRef) ?: return false

        val sipRef = repository.attemptSipFetch(holder.accountBase)
            ?: repository.createSipAccount(userRef, holder.accountBase)
        sipRef.get().await().timber()
        val sipAccount: SipAccount =  repository.getAwaitObject<SipAccount>(sipRef) ?: return false

        if (!user.sipAccountExists()) repository.associateSipAccount(user, userRef, sipRef)

        _storedSipCreds.set(holder)
        _isLoggedIn.set(true)

        userComponent = repository.buildUserComponent(sipAccount, user)

        return true
    }
}