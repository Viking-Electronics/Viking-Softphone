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
import com.vikingelectronics.softphone.util.BasicCallState
import com.vikingelectronics.softphone.util.LinphoneManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.until
import org.linphone.core.*
import java.util.*
import javax.inject.Inject
import kotlin.time.*



@HiltViewModel
@OptIn(ExperimentalTime::class)
class CallViewModel @Inject constructor(
    private val core: Core,
    private val linphoneManager: LinphoneManager
): ViewModel() {

    private val timerJob: Job =
        viewModelScope.launch(Main) {
            repeat(Int.MAX_VALUE) {
                delay(1000)
                callDuration.value = calculateCallDuration()
            }
        }


    private val callInitTime = Clock.System.now()
    private val callTimeNow
        get() = Clock.System.now()

    val callState: Flow<BasicCallState> = linphoneManager.callState.onEach {
        when(it) {
            is BasicCallState.Ending -> timerJob.cancel("Call Ended")
            else -> {}
        }
    }
    val callDuration: MutableState<String> = mutableStateOf("0:00")
    val isMuted = mutableStateOf(true)
    val isEnteringCode = mutableStateOf(false)
    val callError = mutableStateOf(false)

    var relayCode: String by mutableStateOf("")
        private set

    init {
        timerJob.start()
    }

    fun textureViewInflated(textureView: TextureView) {
        core.enableVideoDisplay(true)
        core.nativeVideoWindowId = textureView
    }


    fun answerCall() = linphoneManager.answerCall()

    fun declineCall() = core.currentCall?.decline(Reason.Declined)

    fun endCall() = core.currentCall?.terminate() ?: core.terminateAllCalls()

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

    fun shouldRetryCall(retry: Boolean, device: Device) {
        callError.value = false
        if (retry) linphoneManager.callDevice(device)
    }

    private fun calculateCallDuration(): String = buildString {
        val diff = callTimeNow.epochSeconds - callInitTime.epochSeconds
        diff.toDuration(DurationUnit.SECONDS).toComponents { minutes, seconds, _ ->
            val modifiedSeconds = if (seconds < 10) "0$seconds" else seconds
            append("$minutes:$modifiedSeconds")
        }
    }

}