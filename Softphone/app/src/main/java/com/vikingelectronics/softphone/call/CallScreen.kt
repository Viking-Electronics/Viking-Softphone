package com.vikingelectronics.softphone.call

import android.app.Activity
import android.os.Parcelable
import android.view.TextureView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
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
import androidx.navigation.NavBackStackEntry
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.util.BasicCallState
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize
import org.linphone.core.Call


sealed class CallDirection(open val device: Device): Parcelable {
    @Parcelize class Incoming(override val device: Device): CallDirection(device)
    @Parcelize class Outgoing(override val device: Device): CallDirection(device)

    companion object {
        fun fromCall(call: Call, device: Device): CallDirection {
            return when(call.dir) {
                Call.Dir.Incoming -> Incoming(device)
                Call.Dir.Outgoing -> Outgoing(device)
            }
        }
    }
}
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CallScreen(
    direction: CallDirection,
    navBackStackEntry: NavBackStackEntry? = null
) {
    val viewModel: CallViewModel = navBackStackEntry?.let { hiltViewModel(it) } ?: hiltViewModel()

    (LocalContext.current as AppCompatActivity).apply {
        onBackPressedDispatcher.addCallback(
            LocalLifecycleOwner.current,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.endCall()
                }
            }
        )
    }

    val callState by viewModel.callState.collectAsState(initial = BasicCallState.Waiting)
    val isMuted by viewModel.isMuted
    val isEnteringCode by viewModel.isEnteringCode
    val callError by viewModel.callError

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
                        Text(text = stringResource(R.string.answer))
                    }
                    Button(onClick = viewModel::declineCall) {
                        Text(text = stringResource(R.string.decline))
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
                    Text(text = stringResource(R.string.cancel))
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
                        Text(text = stringResource(R.string.unlock))
                    }
                    Button(onClick = viewModel::endCall) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call icon"
                        )
                        Text(text = stringResource(R.string.hangUp))
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

                    Button(onClick = viewModel::testFailure) {
                        Text(text = "Failure Test")
                    }
                }
            }
            else -> {}
        }
        
        AnimatedVisibility(visible = callError) {
            CallFailedDialog(direction.device) {
                viewModel.shouldRetryCall(it, direction.device)
            }
        }

        AnimatedVisibility(visible = isEnteringCode) {
            val focusRequester = FocusRequester()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0xBBCCCCCC)) //TODO: Convert to color res when we do theming
            ) {
                TextField(
                    value = viewModel.relayCode,
                    onValueChange = viewModel::relayCodeChanged,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .focusRequester(focusRequester),
                    label = {
                        Text(text = stringResource(R.string.relayEntryTextfieldHint))
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
    callState: Flow<BasicCallState>,
    callDuration: State<String>,
    device: Device
) {

    val state by callState.collectAsState(initial = BasicCallState.Waiting)
    val duration by callDuration

    when(state) {
        BasicCallState.Incoming, BasicCallState.Outgoing -> {
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
        BasicCallState.Connected -> {
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
    device: Device,
    shouldRetry: (Boolean) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(text = "Call Error")
        },
        text = {
            Text(text = "There was an issue with connecting to ${device.name}. Retry?")
        },
        confirmButton = {
            Button(onClick = { shouldRetry(true) }) {
                Text(text = stringResource(R.string.retry))
            }

        },
        dismissButton = {
            Button(onClick = { shouldRetry(false) }) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}