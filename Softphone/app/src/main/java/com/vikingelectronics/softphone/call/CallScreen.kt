package com.vikingelectronics.softphone.call

import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import com.vikingelectronics.softphone.databinding.GenericTextureViewBinding
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.extensions.timber
import com.vikingelectronics.softphone.util.LinphoneManager
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.MediaDirection
import org.linphone.mediastream.video.capture.CaptureTextureView
import timber.log.Timber

sealed class CallDirection {
    object Incoming: CallDirection()
    class Outgoing(val device: Device): CallDirection()
}
@Composable
fun CallScreen(
    direction: CallDirection
) {

    val viewModel: CallViewModel = hiltNavGraphViewModel()

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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {

            Button(onClick = viewModel::) {

            }

            Button(
                onClick = viewModel::endCall
            ) {
                Text(text = "END")
            }
        }


    }
}