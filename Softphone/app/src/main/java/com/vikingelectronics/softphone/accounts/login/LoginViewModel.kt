package com.vikingelectronics.softphone.accounts.login

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.vikingelectronics.softphone.util.LinphoneManager
import com.vikingelectronics.softphone.util.PermissionsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import org.linphone.core.TransportType
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val permissionsManager: PermissionsManager,
    private val linphoneManager: LinphoneManager
): ViewModel() {

    var username: String by mutableStateOf("")
        private set
    var userId: String by mutableStateOf("")
        private set
    var password: String by mutableStateOf("")
        private set
    var domain: String by mutableStateOf("")
        private set
    var displayName: String by mutableStateOf("")
        private set
    var transport: TransportType by mutableStateOf(TransportType.Udp)
        private set


    var shouldShowAdvanced by mutableStateOf(false)
        private set

    fun qrClicked(onSuccess:() -> Unit) {
        permissionsManager.requestPermissionsForQRReading(onSuccess)
    }

    fun usernameUpdated(newUsername: String) {
        username = newUsername
    }

    fun userIdUpdated(newId: String) {
        userId = newId
    }

    fun passwordUpdated(newPassword: String) {
        password = newPassword
    }

    fun domainUpdated(newDomain: String) {
        domain = newDomain
    }

    fun displayNameUpdated(newDisplayName: String) {
        displayName = newDisplayName
    }

    fun transportTypeUpdated(newType: TransportType) {
        transport = newType
    }

    fun login(): Boolean = linphoneManager.login(username, password, domain, transport, userId, displayName)

    fun loginTypeSwitch() {
        shouldShowAdvanced = !shouldShowAdvanced
    }
}