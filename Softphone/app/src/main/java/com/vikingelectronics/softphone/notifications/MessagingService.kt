package com.vikingelectronics.softphone.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vikingelectronics.softphone.accounts.LoginRepository
import com.vikingelectronics.softphone.accounts.UserProvider
import com.vikingelectronics.softphone.extensions.timber
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MessagingService: FirebaseMessagingService() {

    @Inject lateinit var userProvider: UserProvider

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        GlobalScope.launch {
            userProvider.updateUserPushToken(p0)
        }
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        p0.data.timber("RemoteMessage")
    }
}