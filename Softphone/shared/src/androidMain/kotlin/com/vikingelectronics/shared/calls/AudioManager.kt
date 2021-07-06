package com.vikingelectronics.shared.calls

import android.media.AudioFocusRequest

actual class AudioManager(private val androidManager: android.media.AudioManager) {
    actual val outputSampleRate: Int
        get() = androidManager.getProperty(android.media.AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE).toInt()
    actual var isSpeakerphoneOn: Boolean = androidManager.isSpeakerphoneOn
    actual var mode: Mode
        get() = Mode.fromInt(androidManager.mode)
        set(mode) { androidManager.mode = mode.value }

    actual fun requestAudioFocus(streamType: StreamType, durationHint: DurationHint): AudioFocusRequestResult {
        val focusRequest = AudioFocusRequest.Builder(streamType.value)
            .setAcceptsDelayedFocusGain(false)
            .build()

        return androidManager.requestAudioFocus(focusRequest).run { AudioFocusRequestResult.fromInt(this) }
    }

    actual fun getStreamVolume(streamType: StreamType): Int = androidManager.getStreamVolume(streamType.value)

    actual fun setStreamVolume(
        streamType: StreamType,
        index: Int,
        flags: Int
    ) = androidManager.setStreamVolume(streamType.value, index, flags)

    actual fun getStreamMaxVolume(streamType: StreamType): Int = androidManager.getStreamMaxVolume(streamType.value)
}

actual sealed class Mode actual constructor(val value: Int) {
    actual object Invalid: Mode(Int.MIN_VALUE)
    actual object Normal: Mode(android.media.AudioManager.MODE_NORMAL)
    actual object Ringtone : Mode(android.media.AudioManager.MODE_RINGTONE)
    actual object InCall : Mode(android.media.AudioManager.MODE_IN_CALL)
    actual object InCommunication : Mode(android.media.AudioManager.MODE_IN_COMMUNICATION)


    companion object {
        fun fromInt(value: Int): Mode
            = Mode::class.sealedSubclasses
            .firstOrNull { it.objectInstance?.value == value }
            ?.objectInstance
            ?: Invalid
    }
}

actual sealed class DurationHint actual constructor(val value: Int) {
    actual object AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE: DurationHint(android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
}

actual sealed class StreamType actual constructor(val value: Int) {
    actual object VoiceCall: StreamType(android.media.AudioManager.STREAM_VOICE_CALL)
}

actual sealed class AudioFocusRequestResult actual constructor(val value: Int) {
    actual object Unknown: AudioFocusRequestResult(Int.MIN_VALUE)
    actual object Failed: AudioFocusRequestResult(android.media.AudioManager.AUDIOFOCUS_REQUEST_FAILED)
    actual object Granted: AudioFocusRequestResult(android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
    actual object Delayed : AudioFocusRequestResult(android.media.AudioManager.AUDIOFOCUS_REQUEST_DELAYED)

    companion object {
        fun fromInt(value: Int): AudioFocusRequestResult
            = AudioFocusRequestResult::class.sealedSubclasses
            .firstOrNull { it.objectInstance?.value == value }
            ?.objectInstance
            ?: Unknown
    }
}