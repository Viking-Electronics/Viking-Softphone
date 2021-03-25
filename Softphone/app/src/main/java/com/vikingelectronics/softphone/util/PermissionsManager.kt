package com.vikingelectronics.softphone.util

import android.Manifest
import android.content.Context
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import org.linphone.core.Core
import javax.inject.Inject
import javax.inject.Singleton

class PermissionsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val core: Core
) {

    private fun listener(onSuccess: () -> Unit): PermissionListener {
        return object : PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                core.restart()
                onSuccess.invoke()
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                TODO("Not yet implemented")
            }

            override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
                TODO("Not yet implemented")
            }
        }
    }

    fun requestPermissionsForQRReading(onSuccess: () -> Unit) {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(listener(onSuccess))
            .check()
    }

    fun requestPermissionsForAudio(onSuccess: () -> Unit) {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.RECORD_AUDIO)
            .withListener(listener(onSuccess))
            .check()
    }

    fun requestPermissionForStorage(onSuccess: () -> Unit) {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(listener(onSuccess))
            .check()
    }
}