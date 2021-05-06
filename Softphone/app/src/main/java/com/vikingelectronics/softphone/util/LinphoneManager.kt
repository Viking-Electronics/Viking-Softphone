package com.vikingelectronics.softphone.util

import android.content.Context
import android.media.AudioManager
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.util.extensions.initIfNull
import com.vikingelectronics.softphone.util.extensions.invokeIfNotNull
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
            username, userId, password, null, null, domain
        ).apply {
            core.addAuthInfo(this)
        }

        val proxyConfig = core.createProxyConfig().apply {
            serverAddr = "<sip:$domain;transport=${transport.name.toLowerCase()}>"
            enableRegister(true)
        }

        val identityAddr = Factory.instance().createAddress(
            "sip:$username@$domain"
        )
        if (identityAddr != null) {
            identityAddr.displayName = displayName
            proxyConfig.identityAddress = identityAddr
        }

        proxyConfig.natPolicy.initIfNull {
            core.createNatPolicy()
        }.run {
            stunServer = context.getString(R.string.default_stun)
            enableStun(true)
            enableIce(true)
            core.natPolicy = this
        }

        return if (core.proxyConfigList.contains(proxyConfig)) true else {
            val proxySetStatus = core.addProxyConfig(proxyConfig) == 0
            core.defaultProxyConfig = proxyConfig
            proxySetStatus //According to docs 0 is success *shrug*
        }
    }

    fun callDevice(scope: CoroutineScope,
                   device: Device) = scope.launch(Main){
        val address = factory.createAddress(device.callAddress)
        val parameters = core.createCallParams(null)?.apply {
            enableVideo(true)
            videoDirection = MediaDirection.RecvOnly
            setAudioBandwidthLimit(0)
        }
        audioManager.isSpeakerphoneOn = true

        invokeIfNotNull(address, parameters) { addr, params ->
            core.inviteAddressWithParams(addr, params)
        }
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