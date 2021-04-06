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
import com.vikingelectronics.softphone.util.extensions.restart
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.linphone.core.Core
import javax.inject.Inject

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

    private suspend fun multiListener(scope: CoroutineScope, onSuccess: suspend () -> Unit): MultiplePermissionsListener {
         return object : MultiplePermissionsListener {
            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                scope.launch {
                    if (p0 != null && p0.areAllPermissionsGranted()) onSuccess.invoke()
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

    suspend fun requestPermissionForStorage(scope: CoroutineScope, onSuccess: suspend () -> Unit) {
        val permissionsList = mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT <= 28) permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        Dexter.withContext(context)
            .withPermissions(permissionsList)
            .withListener(multiListener(scope, onSuccess))
            .check()
    }
}