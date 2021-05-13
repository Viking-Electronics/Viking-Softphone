package com.vikingelectronics.softphone.call

import android.view.TextureView
import androidx.lifecycle.ViewModel
import com.vikingelectronics.softphone.util.LinphoneManager
import dagger.hilt.android.lifecycle.HiltViewModel
import org.linphone.core.Core
import org.linphone.core.VideoActivationPolicy
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val core: Core,
    private val linphoneManager: LinphoneManager
): ViewModel() {

    fun textureViewInflated(textureView: TextureView) {
        core.enableVideoDisplay(true)
        core.nativeVideoWindowId = textureView
    }

    fun answerCall() = linphoneManager.answerCall()

    fun endCall() {
        core.currentCall?.terminate()
    }
}