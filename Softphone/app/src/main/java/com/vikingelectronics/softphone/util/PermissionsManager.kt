package com.vikingelectronics.softphone.util

import android.Manifest
import android.content.Context
import android.os.Build
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
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

    private fun multiListener(onSuccess: () -> Unit): MultiplePermissionsListener {
        return object : MultiplePermissionsListener {
            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                p0?.let {
                    if (it.areAllPermissionsGranted()) onSuccess.invoke()
                }
            }

            override fun onPermissionRationaleShouldBeShown(p0: MutableList<PermissionRequest>?, p1: PermissionToken?) {
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
        val permissionsList = mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT <= 28) permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        Dexter.withContext(context)
            .withPermissions(permissionsList)
            .withListener(multiListener(onSuccess))
            .check()
    }
}