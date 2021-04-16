package com.vikingelectronics.softphone.accounts.login

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import com.tfcporciuncula.flow.FlowSharedPreferences
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.accounts.AccountProvider
import com.vikingelectronics.softphone.accounts.QrReadResult
import com.vikingelectronics.softphone.accounts.StoredSipCredsHolder
import com.vikingelectronics.softphone.extensions.timber
import com.vikingelectronics.softphone.util.LinphoneManager
import com.vikingelectronics.softphone.util.PermissionsManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val accountProvider: AccountProvider
): ViewModel() {

    sealed class Actions {
        class ShouldShowAdvanced(val shouldShow: Boolean): Actions()
        class ShouldScanQrCode(val shouldScan: Boolean): Actions()
        class QrResultsReceived(val qrReadResult: QrReadResult): Actions()
        class RegistrationStatusUpdate(val wasSuccess: Boolean): Actions()

    }

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

//    var
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
            result?.let { parseQrResult(it) }.timber()
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

    fun login(): Boolean = linphoneManager.login(username, password, domain, transport, userId, displayName).also {
        toastId = if (it) R.string.sip_registration_success else R.string.sip_registration_failure
        if (it) accountProvider.setStoredSipCreds(StoredSipCredsHolder(domain, username, password))
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