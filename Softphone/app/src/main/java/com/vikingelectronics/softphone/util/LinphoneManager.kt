package com.vikingelectronics.softphone.util

import android.content.Context
import android.media.AudioManager
import com.vikingelectronics.softphone.call.CallDirection
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.extensions.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import org.linphone.core.*
import org.linphone.core.tools.Log
import javax.inject.Inject
import javax.inject.Singleton

sealed class BasicCallState {
    object Waiting: BasicCallState()
    object Incoming: BasicCallState()
    object Outgoing: BasicCallState()
    object Connected: BasicCallState()
    object Ending: BasicCallState()
    object Failed: BasicCallState()

    companion object {

        fun fromCallState(state: Call.State?): BasicCallState? {
            return when(state) {
                Call.State.Idle -> Waiting
                Call.State.IncomingReceived, Call.State.IncomingEarlyMedia -> Incoming
                Call.State.OutgoingInit, Call.State.OutgoingRinging, Call.State.OutgoingProgress, Call.State.OutgoingEarlyMedia -> Outgoing
                Call.State.Connected -> Connected
                Call.State.Released, Call.State.End -> Ending
                Call.State.Error -> Failed
                else -> null
            }
        }
    }
}

@Singleton
class LinphoneManager @Inject constructor(
    @ApplicationContext val context: Context,
    val factory: Factory,
    val core: Core
) {

    val callState = MutableStateFlow<BasicCallState>(BasicCallState.Waiting)
    val isOnCall = MutableStateFlow(false)
    private val coreCallListener = object: CoreListenerStub() {
        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            super.onCallStateChanged(core, call, state, message)

            GlobalScope.launch {
                BasicCallState.fromCallState(state)?.run { callState.emit(this) }

                if (state == Call.State.IncomingReceived || state == Call.State.OutgoingInit)  {
                    setCallModeToRinging()
                    isOnCall.emit(true)
                }
                if (state == Call.State.Connected) setCallModeToInCall()
                if (state == Call.State.End) {
                    isOnCall.emit(false)
                    setCallModeToNormal()
                }
            }
        }
    }


    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var mEchoTesterIsRunning = false
    private var mAudioFocused = false

    init {
        core.addListener(coreCallListener)
    }

    fun login(
        username: String,
        password: String,
        domain: String,
        transport: TransportType,
        userId: String = "",
        displayName: String = "",
    ): Account? {

        Factory.instance().createAuthInfo(
            username, userId, password, null, null, domain, null
        ).apply {
            core.addAuthInfo(this)
        }

        val address = factory.createAddress("sip:$domain")?.apply {
            this.transport = transport
        }

        val account = core.createAccountWithParams {
            identityAddress = factory.createAddress("sip:$username@$domain")
            serverAddress = address
            registerEnabled = true
        }

        return if(core.accountList.contains(account)) account else {
            //Keep to a single account for now, was causing call issues
            if (core.accountList.isNotEmpty()) {
                core.accountList.forEach {
                    core.removeAccount(it)
                }
            }
            val accountSetStatus = core.addAccount(account) == 0
            core.defaultAccount = account
            if (accountSetStatus) account else null
        }
    }

    fun callDevice(device: Device) {
        val address = factory.createAddress(device.callAddress)
        val parameters = core.createCallParams(null)?.apply {
            enableVideo(true)
            videoDirection = MediaDirection.RecvOnly

            setAudioBandwidthLimit(0)
            enableAudio(true)
            audioDirection = MediaDirection.RecvOnly
        }

        initOrNull(address, parameters) { addr, params ->
            setCallModeToRinging()
            audioManager.isSpeakerphoneOn = true

            core.inviteAddressWithParams(addr, params)
        } ?: kotlin.run { callState.value = BasicCallState.Failed }
    }

    fun answerCall() {
        val call = core.currentCall ?: kotlin.run {
            callState.value = BasicCallState.Failed
            return
        }

        val params = core.createCallParams(call)?.apply {
            enableVideo(true)
            videoDirection = MediaDirection.RecvOnly

            setAudioBandwidthLimit(0)
            enableAudio(true)
            audioDirection = MediaDirection.RecvOnly
        }.timber()

        call.acceptWithParams(params)
    }

    private fun setCallModeToRinging() {
        audioManager.mode = AudioManager.MODE_RINGTONE
        audioManager.isSpeakerphoneOn = true
    }

    private fun setCallModeToInCall() {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    private fun setCallModeToNormal() {
        audioManager.mode = AudioManager.MODE_NORMAL
    }


    fun startEchoTester(): Int {
        val maxVolume: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        val sampleRate: Int = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE).toInt()

        routeAudioToSpeaker(true)
        setCallModeToInCall()
        Log.i("[Manager] Set audio mode on 'Voice Communication'")
        requestAudioFocus(AudioManager.STREAM_VOICE_CALL)

        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVolume, 0)

        core.startEchoTester(sampleRate)
        mEchoTesterIsRunning = true
        return 1
    }

    fun stopEchoTester(): Int {
        mEchoTesterIsRunning = false
        core.stopEchoTester()
        routeAudioToSpeaker(false)
        setCallModeToNormal()
        Log.i("[Manager] Set audio mode on 'Normal'")
        return 1 // status;
    }

    fun getEchoTesterStatus(): Boolean {
        return mEchoTesterIsRunning
    }

    fun routeAudioToSpeaker(speakerOn: Boolean) {
        Log.w(
            "[Manager] Routing audio to "
                + (if (speakerOn) "speaker" else "earpiece")
                + ", disabling bluetooth audio route"
        )
//        org.linphone.receivers.BluetoothManager.getInstance().disableBluetoothSCO()
        audioManager.isSpeakerphoneOn = speakerOn
    }



    private fun requestAudioFocus(stream: Int) {
        if (!mAudioFocused) {
            val res: Int = audioManager.requestAudioFocus(
                null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
            Log.d(
                "[Manager] Audio focus requested: "
                    + if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) "Granted" else "Denied"
            )
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true
        }
    }

    fun startEcCalibration() {
        val oldVolume: Int = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
        val maxVolume: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)

        routeAudioToSpeaker(true)
        setCallModeToInCall()
        Log.i("[Manager] Set audio mode on 'Voice Communication'")
        requestAudioFocus(AudioManager.STREAM_VOICE_CALL)

        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVolume, 0)
        core.startEchoCancellerCalibration()
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, oldVolume, 0)
    }
}