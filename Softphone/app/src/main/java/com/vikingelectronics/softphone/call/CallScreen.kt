package com.vikingelectronics.softphone.call

import android.os.Parcelable
import android.view.TextureView
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.vikingelectronics.softphone.MainActivity
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.devices.Device
import kotlinx.parcelize.Parcelize


sealed class CallDirection(open val device: Device): Parcelable {
    @Parcelize class Incoming(override val device: Device): CallDirection(device)
    @Parcelize class Outgoing(override val device: Device): CallDirection(device)
}
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CallScreen(
    direction: CallDirection,
    onCallEnd: () -> Unit
) {
    val viewModel: CallViewModel = hiltViewModel()
    val callback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            viewModel.endCall()
        }
    }
    (LocalContext.current as MainActivity).apply {
        onBackPressedDispatcher.addCallback(LocalLifecycleOwner.current, callback)
    }

    val callState by viewModel.callState
    val isMuted by viewModel.isMuted
    val isEnteringCode by viewModel.isEnteringCode

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                TextureView(context).apply {
                    viewModel.textureViewInflated(this)
                }
            }
        )

        CallerIdDisplay(callState = viewModel.callState, viewModel.callDuration, direction.device)

        when(callState) {
            is BasicCallState.Waiting -> viewModel.callInitiated(direction, onCallEnd)
            is BasicCallState.Incoming -> {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(vertical = 24.dp)
                        .fillMaxWidth()
                        .height(55.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = viewModel::answerCall) {

                        Text(text = "Answer")
                    }
                    Button(onClick = viewModel::declineCall) {
                        Text(text = "Decline")
                    }
                }
            }
            is BasicCallState.Outgoing -> {
                Button(
                    onClick = viewModel::endCall,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 36.dp, vertical = 24.dp)
                        .fillMaxWidth()
                        .height(55.dp)
                ) {
                    Text(text = "Cancel")
                }
            }
            is BasicCallState.Connected -> {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .fillMaxWidth()
                        .height(55.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = viewModel::relayActivation) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Unlock icon"
                        )
                        Text(text = "Unlock")
                    }
                    Button(onClick = viewModel::endCall) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call icon"
                        )
                        Text(text = "Hang up")
                    }
                    Button(
                        onClick = viewModel::switchMute,
                        shape = CircleShape
                    ) {
                        val tint = if (isMuted) Color.Red else Color.Green
                        Icon(
                            imageVector = Icons.Default.Mic,
                            tint = tint,
                            contentDescription = "Mute icon"
                        )
                    }
                }
            }
            is BasicCallState.Failed -> CallFailedDialog {

            }

            else -> {}
        }

        if (isEnteringCode) {
            val focusRequester = FocusRequester()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0xBBCCCCCC))
            ) {
                TextField(
                    value = viewModel.relayCode,
                    onValueChange = viewModel::relayCodeChanged,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .focusRequester(focusRequester),
                    label = {
                        Text(text = "Enter Relay Activation Code")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onAny = { viewModel.relayCodeEntered() }
                    )
                )
            }
            DisposableEffect(Unit) {
                focusRequester.requestFocus()
                onDispose{ }
            }

        }
    }
}

@Composable
fun BoxScope.CallerIdDisplay(
    callState: State<BasicCallState>,
    callDuration: State<String>,
    device: Device
) {

    val state by callState
    val duration by callDuration

    when(state) {
        is BasicCallState.Incoming, is BasicCallState.Outgoing -> {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
            ) {
                val text = if (state is BasicCallState.Outgoing) {
                    "Connecting to ${device.name}"
                } else "Incoming call from ${device.name}"

                Text(
                    text = text,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = duration,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        is BasicCallState.Connected -> {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {

                Text(
                    text = "Connected to ${device.name}",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = duration,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        else -> {}
    }
}

@Composable
fun CallFailedDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Call Error")
        },
        text = null,
        buttons = {

        }
    )
}