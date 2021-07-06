package com.vikingelectronics.shared.linphone

import kotlinx.coroutines.flow.Flow

expect class Call {
    val state: Flow<State>
    val direction: Direction
    val status: Flow<Status>
    fun enableCamera(enable: Boolean)
    fun acceptWithParams(params: CallParams?)
}

expect enum class Direction {
    Outgoing,
    Incoming;
}

expect enum class Status {
    Success,
    Aborted,
    Missed,
    Declined,
    EarlyAborted,
    AcceptedElsewhere,
    DeclinedElsewhere;
}

expect enum class State {
    Idle,
    IncomingReceived,
    PushIncomingReceived,
    OutgoingInit,
    OutgoingProgress,
    OutgoingRinging,
    OutgoingEarlyMedia,
    Connected,
    StreamsRunning,
    Pausing,
    Paused,
    Resuming,
    Referred,
    Error,
    End,
    PausedByRemote,
    UpdatedByRemote,
    IncomingEarlyMedia,
    Updating,
    Released,
    EarlyUpdatedByRemote,
    EarlyUpdating;
}


expect enum class MediaDirection {
    Invalid,
    Inactive,
    SendOnly,
    RecvOnly,
    SendRecv;
}


expect class CallParams {
    var videoDirection: MediaDirection
    var audioDirection: MediaDirection

    fun enableVideo(shouldEnable: Boolean)
    fun enableAudio(shouldEnable: Boolean)
    fun setAudioBandwidthLimit(limit: Int)
}