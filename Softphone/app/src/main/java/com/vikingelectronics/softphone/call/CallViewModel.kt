package com.vikingelectronics.softphone.call

import android.view.TextureView
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.extensions.invert
import com.vikingelectronics.softphone.extensions.timber
import com.vikingelectronics.softphone.util.LinphoneManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.until
import org.linphone.core.*
import java.util.*
import javax.inject.Inject
import kotlin.time.*

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
@OptIn(ExperimentalTime::class)
class CallViewModel @Inject constructor(
    private val core: Core,
    private val linphoneManager: LinphoneManager
): ViewModel() {

    private val timerJob: Job by lazy {
        viewModelScope.launch(Main) {
            repeat(Int.MAX_VALUE) {
                delay(1000)
                callDuration.value = calculateCallDuration()
            }
        }
    }

    private val callInitTime = Clock.System.now()
    private val callTimeNow
        get() = Clock.System.now()

    val callState: MutableState<BasicCallState> = mutableStateOf(BasicCallState.Waiting)
    val callDuration: MutableState<String> = mutableStateOf("0:00")
    val isMuted = mutableStateOf(true)
    val isEnteringCode = mutableStateOf(false)
    var relayCode: String by mutableStateOf("")
        private set

    lateinit var onCallEnd: () -> Unit
    private val callListener = object: CallListenerStub() {
        override fun onStateChanged(call: Call, state: Call.State?, message: String) {
            super.onStateChanged(call, state, message)
            if (state == Call.State.Connected) callState.value = BasicCallState.Connected
            if (state == Call.State.End) {
                timerJob.cancel()
                linphoneManager.setCallModeToNormal()
                viewModelScope.launch(Main) { onCallEnd() }
                call.removeListener(this)
            }
        }
    }

    fun callInitiated(direction: CallDirection, onCallEnd: () -> Unit) {
        this.onCallEnd = onCallEnd
        callState.value = BasicCallState.fromCallDirection(direction)
        timerJob.start()
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
            linphoneManager.answerCall(callListener)
                ?: kotlin.run { callState.value = BasicCallState.Failed }
        }
    }

    fun declineCall() {
        core.currentCall?.decline(Reason.Declined)
    }

    fun endCall() {
        core.currentCall?.terminate() ?: core.terminateAllCalls()
    }

    fun relayActivation() {
        isEnteringCode.value = true
    }

    fun relayCodeChanged(newCode: String) {
        relayCode = newCode
    }

    fun relayCodeEntered() {
        isEnteringCode.value = false
        viewModelScope.launch(Main) {
            relayCode = buildString {
                for (c in relayCode.toCharArray()) {
                    append("$c ")
                }
            }.trim()
            core.currentCall?.sendDtmfs(relayCode)
            relayCode = ""
        }
    }

    fun switchMute() {
        isMuted.invert()

        val params = core.createCallParams(core.currentCall)?.apply {
            audioDirection = if(isMuted.value) MediaDirection.RecvOnly else MediaDirection.SendRecv
        }

        core.currentCall?.update(params)
    }

    private fun calculateCallDuration(): String = buildString {
        val diff = callTimeNow.epochSeconds - callInitTime.epochSeconds
        diff.toDuration(DurationUnit.SECONDS).toComponents { minutes, seconds, _ ->
            val modifiedSeconds = if (seconds < 10) "0$seconds" else seconds
            append("$minutes:$modifiedSeconds")
        }
    }

}