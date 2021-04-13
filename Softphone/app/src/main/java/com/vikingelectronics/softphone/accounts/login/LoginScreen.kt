package com.vikingelectronics.softphone.accounts.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.navigation.Screen
import com.vikingelectronics.softphone.ui.RadioGroup
import org.linphone.core.TransportType

@Composable
fun LoginScreen(
    navController: NavController
) {

    val viewModel: LoginViewModel = hiltNavGraphViewModel()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column (
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {

        Text(
            text = stringResource(id = R.string.scan_qr_code),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        )
        Button(
            onClick = {
                  viewModel.qrClicked {
                      navController.navigate(Screen.QrCodeReader.route)
                  }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
//            elevation = ButtonElevation.elevation(enabled = false, interactionSource = ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
                .size(100.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.qr_icon),
                contentDescription = stringResource(R.string.qr_button_content_description),
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = stringResource(id = R.string.or),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )


        TextField(
            value = viewModel.username,
            onValueChange = viewModel::usernameUpdated,
            label = { Text(text = stringResource(id = R.string.username)) },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        )

        if (viewModel.shouldShowAdvanced) {
            TextField(
                value = viewModel.userId,
                onValueChange = viewModel::userIdUpdated,
                label = { Text(text = stringResource(id = R.string.user_id_optional)) },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        TextField(
            value = viewModel.password,
            onValueChange = viewModel::passwordUpdated,
            label = { Text(text = stringResource(id = R.string.password)) },
            modifier = Modifier
                .fillMaxWidth()
        )

        TextField(
            value = viewModel.domain,
            onValueChange = viewModel::domainUpdated,
            label = { Text(text = stringResource(id = R.string.domain)) },
            modifier = Modifier
                .fillMaxWidth()
        )

        if (viewModel.shouldShowAdvanced) {
            TextField(
                value = viewModel.displayName,
                onValueChange = viewModel::displayNameUpdated,
                label = { Text(text = stringResource(id = R.string.display_name_optional)) },
                modifier = Modifier
                    .fillMaxWidth()
            )
            
            Text(
                text = stringResource(id = R.string.transport),
                modifier = Modifier.padding(top = 16.dp, start = 16.dp)
            )

            RadioGroup(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                items = mapOf(
                    TransportType.Udp to TransportType.Udp.name,
                    TransportType.Tls to TransportType.Tls.name,
                    TransportType.Tcp to TransportType.Tcp.name
                ),
                defaultItemSelection = viewModel.transport,
                onItemSelect = viewModel::transportTypeUpdated
            )
        }

        Button(
            onClick = {
                if (viewModel.login()) {
                    Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Registration Failed", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.login))
        }

//        Button(
//            onClick = viewModel::loginTypeSwitch,
//            modifier = Modifier
//                .padding(horizontal = 16.dp)
//                .fillMaxWidth()
//        ) {
//            Text(text = stringResource(id = R.string.advanced_login))
//        }
    }
}