package com.vikingelectronics.softphone.call

import android.view.TextureView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.Fragment
import com.vikingelectronics.softphone.databinding.GenericTextureViewBinding
import com.vikingelectronics.softphone.extensions.timber
import com.vikingelectronics.softphone.util.LinphoneManager
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.MediaDirection
import org.linphone.mediastream.video.capture.CaptureTextureView
import timber.log.Timber

@Composable
fun CallScreen(
    core: Core,
    linphoneManager: LinphoneManager
//    call: Call
) {
//    val callListener

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Button(onClick = { core.currentCall?.terminate() }) {
            Text(text = "END")
        }

        AndroidViewBinding(
            GenericTextureViewBinding::inflate,
            modifier = Modifier.fillMaxWidth()
        ) {
            val call = core.currentCall
            val state = call?.state
            if (state == Call.State.IncomingReceived || state == Call.State.OutgoingProgress) {
                core.nativeVideoWindowId = textureView
                core.enableVideoDisplay(true)
            }
            if (call?.state == Call.State.IncomingReceived) {
                linphoneManager.answerCall()
//
//                val params = core.createCallParams(call)?.apply {
//                    enableVideo(true)
//                    videoDirection = MediaDirection.RecvOnly;
//                    setAudioBandwidthLimit(0)
//                }.timber()
//
//                call.acceptWithParams(params)
            }
//            timber()
        }


    }



//    AndroidView(
//        modifier = Modifier.fillMaxSize(),
//        factory = { context ->
//            TextureView(context).apply {
//                val call = core.currentCall
//                if (call?.state == Call.State.IncomingReceived) {
//                    core.nativeVideoWindowId = this
//                    core.enableVideoDisplay(true)
//
////                  call.accept()
//                    val params = core.createCallParams(call)?.apply {
//                        enableVideo(true)
//                        videoDirection = MediaDirection.RecvOnly;
//                        setAudioBandwidthLimit(0)
//                    }.timber()
//
////                  call.update(params)
//                    call.acceptWithParams(params)
//                }
//                timber()
//            }
//        }
//    )
}