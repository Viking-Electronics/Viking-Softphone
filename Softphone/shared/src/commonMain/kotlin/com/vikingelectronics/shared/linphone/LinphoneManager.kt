package com.vikingelectronics.shared.linphone

import com.vikingelectronics.shared.calls.*
import com.vikingelectronics.shared.devices.Device
import com.vikingelectronics.shared.initOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.transform

sealed class BasicCallState {
    object Waiting: BasicCallState()
    object Incoming: BasicCallState()
    object Outgoing: BasicCallState()
    object Connected: BasicCallState()
    object Ending: BasicCallState()
    object Failed: BasicCallState()

    companion object {

        fun fromCallState(state: State?): BasicCallState? {
            return when(state) {
                State.Idle -> Waiting
                State.IncomingReceived, State.IncomingEarlyMedia -> Incoming
                State.OutgoingInit, State.OutgoingRinging, State.OutgoingProgress, State.OutgoingEarlyMedia -> Outgoing
                State.Connected -> Connected
                State.Released, State.End -> Ending
                State.Error -> Failed
                else -> null
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LinphoneManager constructor(
    val audioManager: AudioManager,
    val factory: Factory,
    val core: Core
) {
    val sipRegistrationStatus = core.accountRegistrationState
    val callState = core.callState.transform<State, BasicCallState> {
        BasicCallState.fromCallState(it)?.let { state -> emit(state) }
    }
    val isOnCall = core.callState.transform<State, Boolean> {
        when(it) {
            State.IncomingReceived, State.OutgoingInit -> {
                setCallModeToRinging()
                emit(true)
            }
            State.Connected -> setCallModeToInCall()
            State.End -> {
                setCallModeToNormal()
                emit(false)
            }
        }
    }


    private var mEchoTesterIsRunning = false
    private var mAudioFocused = false

    fun login(
        username: String,
        password: String,
        domain: String,
        transport: TransportType,
        userId: String = "",
        displayName: String = "",
    ): Account? {

        factory.createAuthInfo(
            username = username,
            password = password,
            domain = domain,
            userId = userId
        ).apply {
            core.addAuthInfo(this)
        }

        val address = factory.createAddress("sip:$domain")?.apply {
            this.transport = transport
        }
        val idAddress = factory.createAddress("sip:$username@$domain") ?: return null

        val account = core.createAccountWithParams {
            identityAddress = idAddress
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

            if (core.addAccount(account)) {
                core.defaultAccount = account
                account
            } else null
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
        } //?: kotlin.run { callState.value = BasicCallState.Failed }
    }

    fun answerCall() {
        val call = core.currentCall ?: kotlin.run {
//            callState.
//            callState.value = BasicCallState.Failed
            return
        }

        val params = core.createCallParams(call)?.apply {
            enableVideo(true)
            videoDirection = MediaDirection.RecvOnly

            setAudioBandwidthLimit(0)
            enableAudio(true)
            audioDirection = MediaDirection.RecvOnly
        }//.timber()

        call.acceptWithParams(params)
    }

    private fun setCallModeToRinging() {
        audioManager.mode = Mode.Ringtone
        audioManager.isSpeakerphoneOn = true
    }

    private fun setCallModeToInCall() {
        audioManager.mode = Mode.InCall
    }

    private fun setCallModeToNormal() {
        audioManager.mode = Mode.Normal
    }


    fun startEchoTester(): Int {
        val maxVolume: Int = audioManager.getStreamMaxVolume(StreamType.VoiceCall)
        val sampleRate: Int = audioManager.outputSampleRate

        routeAudioToSpeaker(true)
        setCallModeToInCall()
//        Log.i("[Manager] Set audio mode on 'Voice Communication'")
        requestAudioFocus(StreamType.VoiceCall)

        audioManager.setStreamVolume(StreamType.VoiceCall, maxVolume, 0)

        core.startEchoTester(sampleRate)
        mEchoTesterIsRunning = true
        return 1
    }

    fun stopEchoTester(): Int {
        mEchoTesterIsRunning = false
        core.stopEchoTester()
        routeAudioToSpeaker(false)
        setCallModeToNormal()
//        Log.i("[Manager] Set audio mode on 'Normal'")
        return 1 // status;
    }

    fun getEchoTesterStatus(): Boolean {
        return mEchoTesterIsRunning
    }

    fun routeAudioToSpeaker(speakerOn: Boolean) {
//        Log.w(
//            "[Manager] Routing audio to "
//                + (if (speakerOn) "speaker" else "earpiece")
//                + ", disabling bluetooth audio route"
//        )
//        org.linphone.receivers.BluetoothManager.getInstance().disableBluetoothSCO()
        audioManager.isSpeakerphoneOn = speakerOn
    }



    private fun requestAudioFocus(streamType: StreamType) {
        if (!mAudioFocused) {
            val res: AudioFocusRequestResult = audioManager.requestAudioFocus(streamType, DurationHint.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            if (res == AudioFocusRequestResult.Granted) mAudioFocused = true
        }
    }

    fun startEcCalibration() {
        val oldVolume: Int = audioManager.getStreamVolume(StreamType.VoiceCall)
        val maxVolume: Int = audioManager.getStreamMaxVolume(StreamType.VoiceCall)

        routeAudioToSpeaker(true)
        setCallModeToInCall()
//        Log.i("[Manager] Set audio mode on 'Voice Communication'")
        requestAudioFocus(StreamType.VoiceCall)

        audioManager.setStreamVolume(StreamType.VoiceCall, maxVolume, 0)
        core.startEchoCancellerCalibration()
        audioManager.setStreamVolume(StreamType.VoiceCall, oldVolume, 0)
    }
}