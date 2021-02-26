package com.vikingelectronics.softphone.util

import android.content.Context
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.accounts.add.AccountAddState
import dagger.hilt.android.qualifiers.ApplicationContext
import org.linphone.LinphoneManager
import org.linphone.core.*
import javax.inject.Inject

class LinphoneManager @Inject constructor(
    @ApplicationContext val context: Context,
    val factory: Factory,
    val core: Core
) {

    fun login(
        username: String,
        password: String,
        domain: String,
        transport: TransportType,
        userId: String = "",
        displayName: String = "",
    ) {

        Factory.instance().createAuthInfo(
            username, userId, password, null, null, domain
        ).apply {
            core.addAuthInfo(this)
        }

        val proxyConfig = core.createProxyConfig().apply {
            serverAddr = "<sip:$domain;transport=${transport.name.toLowerCase()}>"
        }

        val identityAddr = Factory.instance().createAddress(
            "sip:$username@$domain"
        )
        if (identityAddr != null) {
            identityAddr.displayName = displayName
            proxyConfig.identityAddress = identityAddr
        }

        var natPolicy = proxyConfig.natPolicy
        if (natPolicy == null) {
            natPolicy = core.createNatPolicy()
            natPolicy.stunServer = context.getString(R.string.default_stun)
            natPolicy.enableStun(true)
            natPolicy.enableIce(true)
            core.natPolicy = natPolicy
        }

        core.addProxyConfig(proxyConfig)
        core.defaultProxyConfig = proxyConfig

    }
}