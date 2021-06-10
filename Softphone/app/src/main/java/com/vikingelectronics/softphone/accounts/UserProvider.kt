package com.vikingelectronics.softphone.accounts

import com.squareup.moshi.Moshi
import com.tfcporciuncula.flow.FlowSharedPreferences
import com.tfcporciuncula.flow.NullableSerializer
import com.vikingelectronics.softphone.dagger.UserComponent
import com.vikingelectronics.softphone.dagger.UserComponentEntryPoint
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.extensions.nonSettable
import com.vikingelectronics.softphone.networking.ActivityRepository
import com.vikingelectronics.softphone.networking.CapturesRepository
import com.vikingelectronics.softphone.networking.DeviceRepository
import com.vikingelectronics.softphone.schedules.SchedulesRepository
import dagger.hilt.EntryPoints
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

interface RepositoryProvider {
    val deviceRepository: DeviceRepository
    val activityRepository: ActivityRepository
    val capturesRepository: CapturesRepository
    val schedulesRepository: SchedulesRepository
}

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class UserProvider @OptIn(ExperimentalCoroutinesApi::class) @Inject constructor(
    preferences: FlowSharedPreferences,
    moshi: Moshi,
    private val userComponentProvider: Provider<UserComponent.Builder>,
    private val repository: LoginRepository
): RepositoryProvider {

    private val credsAdapter = moshi.adapter(StoredSipCredentials::class.java)
    private val storedCredsSerializer = object: NullableSerializer<StoredSipCredentials> {
        override fun deserialize(serialized: String?): StoredSipCredentials? = credsAdapter.fromJson(serialized)
        override fun serialize(value: StoredSipCredentials?): String? = credsAdapter.toJson(value)
    }


    private val _isLoggedIn = preferences.getBoolean("isLoggedIn", false)
    val isLoggedIn = _isLoggedIn.nonSettable()
    private val _storedSipCreds = preferences.getNullableObject("stored_sip_creds", storedCredsSerializer, null)
    val storedSipCreds = _storedSipCreds.nonSettable()

    private var userComponent: UserComponent? = null
    private val userComponentEntryPoint: UserComponentEntryPoint
        get() = EntryPoints.get(userComponent, UserComponentEntryPoint::class.java)

    override val deviceRepository: DeviceRepository
        get() = userComponentEntryPoint.deviceRepository()
    override val activityRepository: ActivityRepository
        get() = userComponentEntryPoint.activityRepository()
    override val capturesRepository: CapturesRepository
        get() = userComponentEntryPoint.capturesRepository()
    override val schedulesRepository: SchedulesRepository
        get() = userComponentEntryPoint.schedulesRepository()


    suspend fun checkStoredSipCreds() {
        _isLoggedIn.set(_storedSipCreds.isSet())

        val creds = _storedSipCreds.get()
        if (_isLoggedIn.get() && creds != null) userAuthenticatedSuccessfully(creds)

    }

    suspend fun userAuthenticatedSuccessfully(holder: StoredSipCredentials): Boolean {
        val userRepresentation = repository.fetchOrCreateUserAccount(holder.username)
        val user = userRepresentation.getObj() ?: return false

        val sipRepresentation = repository.fetchOrCreateSipAccount(userRepresentation.reference, holder.accountBase)
        val sipAccount = sipRepresentation.getObj() ?: return false

        repository.associateSipAccountIfNecessary(user, userRepresentation, sipRepresentation)

        _storedSipCreds.set(holder)
        _isLoggedIn.set(true)

        userComponent = buildComponent(user, sipAccount)

        return true
    }

    fun rebindSipAccountWithDevicesIfNecessary(user: User, sipAccount: SipAccount, devices: List<Device>) {
        if (sipAccount.deviceObjects.containsAll(devices)) return

        val newSip = sipAccount.copy(deviceObjects = devices)

        userComponent = buildComponent(user, newSip)
    }

    private fun buildComponent(user: User, sipAccount: SipAccount): UserComponent
        = userComponentProvider.get().setUser(user).setSip(sipAccount).build()
}