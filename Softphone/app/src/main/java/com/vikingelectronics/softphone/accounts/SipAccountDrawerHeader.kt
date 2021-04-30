package com.vikingelectronics.softphone.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.linphone.core.RegistrationState

@Composable
fun SipAccountDrawerHeader(
    userProvider: UserProvider
) {
    val creds = userProvider.storedSipCreds.get()
    val color  = when(userProvider.sipRegistrationStatus) {
        RegistrationState.Ok -> Color.Green
        RegistrationState.Progress -> Color.Yellow
        RegistrationState.Failed -> Color.Red
        else -> Color.LightGray
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.LightGray)
    ) {

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = creds?.displayName ?: creds?.username ?: "Something went wrong",
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${creds?.username}@${creds?.domain}",
                fontSize = 12.sp,
                color = Color.Red
            )
        }

        Box(
            modifier = Modifier
                .padding(end = 16.dp)
                .size(16.dp)
                .background(Color.White, shape = CircleShape)
                .align(Alignment.CenterEnd)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, shape = CircleShape)
                    .align(Alignment.Center)
            )
        }
    }
}