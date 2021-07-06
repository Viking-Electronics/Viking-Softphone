package com.vikingelectronics.softphone.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.moshi.Moshi
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.accounts.UserProvider
import com.vikingelectronics.softphone.call.CallActivity
import com.vikingelectronics.shared.devices.Device
import com.vikingelectronics.softphone.extensions.timber
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MessagingService: FirebaseMessagingService() {

    @Inject lateinit var userProvider: UserProvider
    @Inject lateinit var manager: NotificationManager
    @Inject lateinit var moshi: Moshi


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        GlobalScope.launch {
            userProvider.updateUserPushToken(p0)
        }
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        p0.data.timber("RemoteMessage")
        if (p0.data["device"] != null) notifyCall(p0)
    }


    private fun notifyCall(message: RemoteMessage) {
        NotificationChannel("CALL_CHANNEL", "Calls", NotificationManager.IMPORTANCE_HIGH).apply {
            this.lightColor = Color.Cyan.toArgb()
            manager.createNotificationChannel(this)
        }

        val device = moshi.adapter(Device::class.java).fromJson(message.data.getOrDefault("device",""))
        val pendingCallIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, CallActivity::class.java).apply {
                  putExtra("device", device)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, "CALL_CHANNEL").apply {
            setContentTitle(message.data.getOrDefault("title", "defaultTitle"))
            setContentText(message.data.getOrDefault("body", "defaultBody"))
            priority = NotificationCompat.PRIORITY_HIGH
            setCategory(NotificationCompat.CATEGORY_CALL)
            setSmallIcon(R.drawable.viking_logo_round)
            setFullScreenIntent(pendingCallIntent, true)
//            setDeleteIntent()
            setTimeoutAfter(30000L)
            setAutoCancel(true)
            setSound(RingtoneManager.getValidRingtoneUri(this@MessagingService))
        }.build()


        startForeground(1111, notification)
//        manager.notify(1111, notification)
    }

    private fun notifyActivity(message: RemoteMessage) {

    }
}