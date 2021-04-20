package com.vikingelectronics.softphone.accounts

import com.squareup.moshi.Moshi
import com.tfcporciuncula.flow.FlowSharedPreferences
import com.tfcporciuncula.flow.NullableSerializer
import com.vikingelectronics.softphone.extensions.nonSettable
import javax.inject.Inject

class AccountProvider @Inject constructor(
    preferences: FlowSharedPreferences,
    moshi: Moshi
) {

    private val credsAdapter = moshi.adapter(StoredSipCredsHolder::class.java)
    private val storedCredsSerializer = object: NullableSerializer<StoredSipCredsHolder> {
        override fun deserialize(serialized: String?): StoredSipCredsHolder? = credsAdapter.fromJson(serialized)
        override fun serialize(value: StoredSipCredsHolder?): String? = credsAdapter.toJson(value)
    }


    private val _isLoggedIn = preferences.getBoolean("isLoggedIn", false)
    val isLoggedIn = _isLoggedIn.nonSettable()
    private val _storedSipCreds = preferences.getNullableObject("stored_sip_creds", storedCredsSerializer, null)
    val storedSipCreds = _storedSipCreds.nonSettable()


    fun checkStoredSipCreds() {
        _isLoggedIn.set(_storedSipCreds.isSet())
    }

    fun setStoredSipCreds(holder: StoredSipCredsHolder) {
        _storedSipCreds.set(holder)
        _isLoggedIn.set(true)
    }
}