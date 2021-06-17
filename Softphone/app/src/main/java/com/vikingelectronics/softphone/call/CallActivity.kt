package com.vikingelectronics.softphone.call

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.vikingelectronics.softphone.devices.Device
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallActivity: AppCompatActivity() {

    val device: Device?
        get() = intent.extras?.getParcelable("device")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    } else {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

        device ?: finish()
        setContent {

            CallScreen(direction = CallDirection.Incoming(device!!), null)
        }
    }
}