package com.vikingelectronics.shared.calls

expect class AudioManager {
    val outputSampleRate: Int
    var isSpeakerphoneOn: Boolean
    var mode: Mode

    fun requestAudioFocus(streamType: StreamType, durationHint: DurationHint): AudioFocusRequestResult

    fun getStreamVolume(streamType: StreamType): Int
    fun setStreamVolume(streamType: StreamType, index: Int, flags: Int)
    fun getStreamMaxVolume(streamType: StreamType): Int
}

expect sealed class Mode(value: Int) {
    object Invalid: Mode
    object Normal: Mode
    object Ringtone: Mode
    object InCall: Mode
    object InCommunication: Mode
}

expect sealed class DurationHint(value: Int) {
    object AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE: DurationHint
}

expect sealed class StreamType(value: Int) {
    object VoiceCall: StreamType
}

expect sealed class AudioFocusRequestResult(value: Int) {
    object Unknown: AudioFocusRequestResult
    object Failed: AudioFocusRequestResult
    object Granted: AudioFocusRequestResult
    object Delayed: AudioFocusRequestResult
}

