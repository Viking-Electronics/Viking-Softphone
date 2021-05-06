package com.vikingelectronics.softphone.call

import android.view.TextureView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import com.vikingelectronics.softphone.extensions.timber
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.mediastream.video.capture.CaptureTextureView
import timber.log.Timber

@Composable
fun CallScreen(
    core: Core,
//    call: Call
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            CaptureTextureView(context).apply {
                core.nativeVideoWindowId = this
//                core.nativePreviewWindowId = this

                val call = core.currentCall
                val params = call?.params?.apply {
                    enableVideo(true)
                }
//            val params = core.createCallParams(call)
//            params?.enableVideo(true)
                core.enableVideoDisplay(true)

                call?.params?.receivedVideoDefinition?.name.timber()

                if (call?.state == Call.State.IncomingReceived) call.acceptWithParams(params)
            }
        }
    )
}