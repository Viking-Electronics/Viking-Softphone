package com.vikingelectronics.softphone.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.squareup.moshi.Moshi
import com.tfcporciuncula.flow.FlowSharedPreferences
import com.tfcporciuncula.flow.NullableSerializer
import com.tfcporciuncula.flow.Preference
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import com.vikingelectronics.softphone.accounts.StoredSipCredsHolder

class AccountProvider @Inject constructor(
    preferences: FlowSharedPreferences,
    moshi: Moshi
) {

    private val credsAdapter = moshi.adapter(StoredSipCredsHolder::class.java)
    private val storedCredsSerializer = object: NullableSerializer<StoredSipCredsHolder> {
        override fun deserialize(serialized: String?): StoredSipCredsHolder? = credsAdapter.fromJson(serialized)

        override fun serialize(value: StoredSipCredsHolder?): String? = credsAdapter.toJson(value)

    }

    class NonSettablePreference<T>(val preference: Preference<T>) {
        fun asFlow(): Flow<T> = preference.asFlow()
        fun get(): T = preference.get()
        fun isNotSet(): Boolean = preference.isNotSet()
        fun isSet(): Boolean = preference.isSet()
        @Composable
        fun collectAsFlowState(): State<T> = preference.asFlow().collectAsState(initial = get())
    }

    private val _isLoggedIn = preferences.getBoolean("isLoggedIn", false)
    val isLoggedIn: NonSettablePreference<Boolean> = NonSettablePreference(_isLoggedIn)
    private val storedSipCreds = preferences.getNullableObject("stored_sip_creds", storedCredsSerializer, null)

    fun checkStoredSipCreds() {
        _isLoggedIn.set(storedSipCreds.isSet())
    }

    fun setStoredSipCreds(holder: StoredSipCredsHolder) {
        storedSipCreds.set(holder)
        _isLoggedIn.set(true)
    }

}