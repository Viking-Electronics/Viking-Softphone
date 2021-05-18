package com.vikingelectronics.softphone.call

import android.view.TextureView
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.extensions.invert
import com.vikingelectronics.softphone.util.LinphoneManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.linphone.core.*
import javax.inject.Inject

sealed class BasicCallState {
    object Waiting: BasicCallState()
    object Incoming: BasicCallState()
    object Outgoing: BasicCallState()
    object Connected: BasicCallState()
    object Failed: BasicCallState()

    companion object {
        fun fromCallDirection(direction: CallDirection): BasicCallState {
            return when(direction) {
                is CallDirection.Incoming -> Incoming
                is CallDirection.Outgoing -> Outgoing
            }
        }
    }
}

@HiltViewModel
class CallViewModel @Inject constructor(
    private val core: Core,
    private val linphoneManager: LinphoneManager
): ViewModel() {

    internal val callState: MutableState<BasicCallState> = mutableStateOf(BasicCallState.Waiting)
    val isMuted = mutableStateOf(true)

    lateinit var onCallEnd: () -> Unit
    private val callListener = object: CallListenerStub() {
        override fun onStateChanged(call: Call, state: Call.State?, message: String) {
            super.onStateChanged(call, state, message)
            if (state == Call.State.Connected) callState.value = BasicCallState.Connected
            if (state == Call.State.End) {
                linphoneManager.setCallModeToNormal()
                viewModelScope.launch(Main) { onCallEnd() }
                call.removeListener(this)
            }
        }
    }

    fun callInitiated(direction: CallDirection, onCallEnd: () -> Unit) {
        this.onCallEnd = onCallEnd
        callState.value = BasicCallState.fromCallDirection(direction)
        if (callState.value is BasicCallState.Outgoing) callDevice(direction.device)
    }

    fun textureViewInflated(textureView: TextureView) {
        core.enableVideoDisplay(true)
        core.nativeVideoWindowId = textureView
    }

    private fun callDevice(device: Device) {
        viewModelScope.launch(Main) {
            linphoneManager.callDevice(device)?.apply {
                addListener(callListener)
            } ?: kotlin.run { callState.value = BasicCallState.Failed }
        }
    }

    fun answerCall() {
        viewModelScope.launch(Main) {
            linphoneManager.answerCall()?.apply {
                addListener(callListener)
            } ?: kotlin.run { callState.value = BasicCallState.Failed }
        }
    }

    fun declineCall() {
        core.currentCall?.decline(Reason.Declined)
    }

    fun endCall() {
        core.currentCall?.terminate() ?: core.terminateAllCalls()
    }

    fun relayActivation() {
        viewModelScope.launch(Main) {
            repeat(2) {
                core.playDtmf('*', 500)
                delay(200)
            }
        }
    }

    fun switchMute() {
        isMuted.invert()

        val params = core.createCallParams(core.currentCall)?.apply {
            audioDirection = if(isMuted.value) MediaDirection.RecvOnly else MediaDirection.SendRecv
        }

        core.currentCall?.update(params)
    }

}