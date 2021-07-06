package com.vikingelectronics.shared.linphone

import kotlinx.coroutines.channels.awaitClose
import org.linphone.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


actual class Call internal constructor(val android: org.linphone.core.Call) {
    actual val direction: Direction
        get() = Direction.values()[android.dir.ordinal]

    actual val state: Flow<State> = callbackFlow {
        val listener = object: CallListenerStub() {
            override fun onStateChanged(call: org.linphone.core.Call, state: org.linphone.core.Call.State?, message: String) {
                super.onStateChanged(call, state, message)
                state?.ordinal?.let { ord ->
                    trySend(State.values()[ord])
                }
            }
        }
        android.addListener(listener)
        awaitClose { android.removeListener(listener) }
    }

    actual val status: Flow<Status> = callbackFlow {
        android.transferState
    }

    actual fun enableCamera(enable: Boolean) = android.enableCamera(enable)
    actual fun acceptWithParams(params: CallParams?) = android.acceptWithParams(params?.android).run { Unit }
}

actual typealias Status = org.linphone.core.Call.Status
actual typealias Direction = org.linphone.core.Call.Dir
actual typealias State = org.linphone.core.Call.State

actual typealias MediaDirection = org.linphone.core.MediaDirection

actual class CallParams internal constructor(val android: org.linphone.core.CallParams) {
    actual var videoDirection: MediaDirection = android.videoDirection
    actual var audioDirection: MediaDirection = android.audioDirection

    actual fun enableVideo(shouldEnable: Boolean) = android.enableVideo(shouldEnable)
    actual fun enableAudio(shouldEnable: Boolean) = android.enableAudio(shouldEnable)
    actual fun setAudioBandwidthLimit(limit: Int) = android.setAudioBandwidthLimit(limit)
}

