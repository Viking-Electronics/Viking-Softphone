package com.vikingelectronics.softphone.accounts.login

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.accounts.UserProvider
import com.vikingelectronics.softphone.accounts.QrReadResult
import com.vikingelectronics.softphone.accounts.StoredSipCredsHolder
import com.vikingelectronics.softphone.extensions.timber
import com.vikingelectronics.softphone.util.LinphoneManager
import com.vikingelectronics.softphone.util.PermissionsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.TransportType
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val permissionsManager: PermissionsManager,
    private val linphoneManager: LinphoneManager,
    private val core: Core,
    private val moshi: Moshi,
    private val userProvider: UserProvider
): ViewModel() {

    private var usernameBase: String = ""
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
    var transport: TransportType by mutableStateOf(TransportType.Tcp)
        private set

    var loginSuccessful by mutableStateOf(false)
        private set

    var toastId: Int? by mutableStateOf(null)
        private set
    var shouldShowAdvanced by mutableStateOf(false)
        private set
    var shouldScanQrCode by mutableStateOf(false)
        private set
    var qrResults: QrReadResult? by mutableStateOf(null)
        private set

    private val qrStubListener = object: CoreListenerStub() {
        override fun onQrcodeFound(core: Core, result: String?) {
            super.onQrcodeFound(core, result)
            parseQrResult(result)
            qrDeflated()
        }
    }

    fun qrClicked() {
        permissionsManager.requestPermissionsForQRReading { shouldScanQrCode = true }
    }

    fun qrInflated(setNativePreviewWindowId: (Core) -> Unit) {
        setBackCamera()

        setNativePreviewWindowId(core)
        core.enableQrcodeVideoPreview(true)
        core.enableVideoPreview(true)
        core.addListener(qrStubListener)
    }

    fun parseQrResult(result: String?) {
        result ?: return

        val cleanedJson = result.replace("'[", "[")
            .replace("]'", "]")
            .replace("'", "\"").timber()
        moshi.adapter(QrReadResult::class.java).fromJson(cleanedJson)?.let {
            qrResults = it
        }
    }

    fun qrDeflated() {
        shouldScanQrCode = false

        core.nativePreviewWindowId = null
        core.enableQrcodeVideoPreview(false)
        core.enableVideoPreview(false)
        core.removeListener(qrStubListener)
    }

    fun killQrResults() {
        qrResults = null
    }

    fun userBaseUpdated(newBase: String) {
        usernameBase = newBase
    }

    fun usernameUpdated(newUsername: String) {
        username = newUsername.trim()
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

    fun login() {
        if (usernameBase.isEmpty() && domain == "sip.myviking.com:5799") usernameBase = username.substring(0, 10)
        if (usernameBase.isEmpty()) usernameBase = username

        viewModelScope.launch {
            val registrationSuccess = linphoneManager.login(username, password, domain, transport, userId, displayName)

            loginSuccessful = if (registrationSuccess) {
                userProvider.userAuthenticatedSuccessfully(StoredSipCredsHolder(usernameBase, domain, username, password))
            } else false

            toastId = if (loginSuccessful)  R.string.sip_registration_success else R.string.sip_registration_failure
        }
    }

    fun loginTypeSwitch() {
        shouldShowAdvanced = !shouldShowAdvanced
    }

    private fun setBackCamera() {
        var firstDevice: String? = null
        for (camera in core.videoDevicesList) {
            if (firstDevice == null) {
                firstDevice = camera
            }
            if (camera.contains("Back")) {
                core.videoDevice = camera.timber("Found back facing camera:")
                return
            }
        }

        core.videoDevice = firstDevice.timber("Using first camera available:")
    }
}