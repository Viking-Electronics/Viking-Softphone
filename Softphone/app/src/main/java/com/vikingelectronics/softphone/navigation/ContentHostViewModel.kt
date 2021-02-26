package com.vikingelectronics.softphone.navigation

import androidx.lifecycle.ViewModel
import com.vikingelectronics.softphone.util.getSafeDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.ProxyConfig
import org.linphone.core.RegistrationState
import javax.inject.Inject


@HiltViewModel
class ContentHostViewModel @Inject constructor(
    val core: Core
): ViewModel(){


    private val coreRegistrationListener = object : CoreListenerStub() {
        override fun onRegistrationStateChanged(lc: Core?, cfg: ProxyConfig?, cstate: RegistrationState?, message: String?) {
            super.onRegistrationStateChanged(lc, cfg, cstate, message)

        }
    }

    init {
        core.addListener(coreRegistrationListener)
    }

    override fun onCleared() {
        core.removeListener(coreRegistrationListener)
        super.onCleared()
    }

    private val proxy: ProxyConfig?
        get() = core.defaultProxyConfig

    val displayName: StateFlow<String> = MutableStateFlow(proxy?.identityAddress?.getSafeDisplayName() ?: "No display name set")
    val address: StateFlow<String> = MutableStateFlow(proxy?.identityAddress?.asStringUriOnly() ?: "No address configured")
}