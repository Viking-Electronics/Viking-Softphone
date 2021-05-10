package com.vikingelectronics.softphone.util

import android.content.Context
import android.media.AudioManager
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.extensions.initIfNull
import com.vikingelectronics.softphone.extensions.invokeIfNotNull
import com.vikingelectronics.softphone.extensions.timber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.linphone.core.*
import org.linphone.core.tools.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinphoneManager @Inject constructor(
    @ApplicationContext val context: Context,
    val factory: Factory,
    val core: Core
) {

    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var mEchoTesterIsRunning = false
    private var mAudioFocused = false

    fun login(
        username: String,
        password: String,
        domain: String,
        transport: TransportType,
        userId: String = "",
        displayName: String = "",
    ): Boolean {

        Factory.instance().createAuthInfo(
            username, userId, password, null, null, domain, null
        ).apply {
            core.addAuthInfo(this)
        }

        val address = factory.createAddress("sip:$domain")?.apply {
            this.transport = transport
        }

        val params = core.createAccountParams().apply {
            identityAddress = factory.createAddress("sip:$username@$domain")
            serverAddress = address
            registerEnabled = true
        }

        val account = core.createAccount(params)

        return if(core.accountList.contains(account)) true else {
            val accountSetStatus = core.addAccount(account) == 0
            core.defaultAccount = account
            accountSetStatus
        }
    }

    fun callDevice(scope: CoroutineScope,
                   device: Device) = scope.launch(Main){
        val address = factory.createAddress(device.callAddress)
        val parameters = core.createCallParams(null)?.apply {
            enableVideo(true)
            videoDirection = MediaDirection.RecvOnly
            setAudioBandwidthLimit(0)
            enableAudio(true)
        }
        audioManager.isSpeakerphoneOn = true
        setAudioManagerInCallMode()

        invokeIfNotNull(address, parameters) { addr, params ->
            core.inviteAddressWithParams(addr, params)
        }
    }

    fun answerCall() {
        val call = core.currentCall

        val params = core.createCallParams(call)?.apply {
            enableVideo(true)
            videoDirection = MediaDirection.RecvOnly

            setAudioBandwidthLimit(0)
            enableAudio(true)
            audioDirection = MediaDirection.RecvOnly
        }.timber()

        audioManager.isSpeakerphoneOn = true
        setAudioManagerInCallMode()

        call?.acceptWithParams(params)
    }


    fun startEchoTester(): Int {
        routeAudioToSpeaker(true)
        setAudioManagerInCallMode()
        Log.i("[Manager] Set audio mode on 'Voice Communication'")
        requestAudioFocus(AudioManager.STREAM_VOICE_CALL)
        val maxVolume: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVolume, 0)
        val sampleRateProperty: String = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
        val sampleRate: Int = sampleRateProperty.toInt()
        core.startEchoTester(sampleRate)
        mEchoTesterIsRunning = true
        return 1
    }

    fun stopEchoTester(): Int {
        mEchoTesterIsRunning = false
        core.stopEchoTester()
        routeAudioToSpeaker(false)
        audioManager.mode = AudioManager.MODE_NORMAL
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
        enableSpeaker(speakerOn)
    }

    fun enableSpeaker(enable: Boolean) {
        audioManager.isSpeakerphoneOn = enable
    }

    private fun setAudioManagerInCallMode() {
        if (audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            Log.w("[Manager][AudioManager] already in MODE_IN_COMMUNICATION, skipping...")
            return
        }
        Log.d("[Manager][AudioManager] Mode: MODE_IN_COMMUNICATION")
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
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
        routeAudioToSpeaker(true)
        setAudioManagerInCallMode()
        Log.i("[Manager] Set audio mode on 'Voice Communication'")
        requestAudioFocus(AudioManager.STREAM_VOICE_CALL)
        val oldVolume: Int = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
        val maxVolume: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVolume, 0)
        core.startEchoCancellerCalibration()
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, oldVolume, 0)
    }
}