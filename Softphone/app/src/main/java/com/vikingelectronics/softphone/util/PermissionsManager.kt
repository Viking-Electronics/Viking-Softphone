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
import org.linphone.core.Core
import javax.inject.Inject

class PermissionsManager @Inject constructor(
    @ActivityContext private val context: Context,
    private val core: Core
) {

    fun requestPermissionsForQRReading(onSuccess: () -> Unit) {
        val permissionListener =  object : PermissionListener {
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
        Dexter.withContext(context)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(permissionListener)
            .check()
    }
}