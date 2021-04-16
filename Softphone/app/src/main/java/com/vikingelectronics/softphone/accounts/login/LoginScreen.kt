package com.vikingelectronics.softphone.accounts.login

import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.vikingelectronics.softphone.MainActivity
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.accounts.QrReadResult
import com.vikingelectronics.softphone.databinding.GenericTextureViewBinding
import com.vikingelectronics.softphone.navigation.Screen
import com.vikingelectronics.softphone.ui.RadioGroup
import org.linphone.core.TransportType

@Composable
fun LoginScreen(
    navController: NavController
) {

    val viewModel: LoginViewModel = hiltNavGraphViewModel()
    val scrollState = rememberScrollState()
    val callback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (viewModel.shouldScanQrCode) {
                viewModel.qrDeflated()
            } else navController.navigateUp()
        }
    }
    val context = (LocalContext.current as MainActivity).apply {
        onBackPressedDispatcher.addCallback(LocalLifecycleOwner.current, callback)
    }

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
            onClick = viewModel::qrClicked,
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
//                    Toast.makeText(context, stringResource(R.string.sip_registration_success), Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.Primary.DeviceList.route)
                } else {
//                    Toast.makeText(context, stringResource(R.string.sip_registration_failure), Toast.LENGTH_SHORT).show()
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

//    when(viewModel.)

    if (viewModel.shouldScanQrCode) QrReaderView(viewModel = viewModel)

    if (viewModel.qrResults != null) QrResultsAlert(viewModel = viewModel)

    if (viewModel.toastId != null) Toast.makeText(context, viewModel.toastId ?: R.string.empty_string_res, Toast.LENGTH_SHORT).show()
}

@Composable
fun QrReaderView(
    viewModel: LoginViewModel
) {
    AndroidViewBinding(
        GenericTextureViewBinding::inflate,
        modifier = Modifier.fillMaxSize()
    ) {
        viewModel.qrInflated { core ->
            core.nativePreviewWindowId = this.textureView
        }
    }
}

@Composable
fun QrResultsAlert(
    viewModel: LoginViewModel,
) {
    AlertDialog(
        onDismissRequest = {
            viewModel.killQrResults()
        },
        title = {
            Text(text = stringResource(R.string.qr_results_alert_title))
        },
        text = null,
        buttons = {
            LazyColumn(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                val results = viewModel.qrResults?.forEasyConsumption() ?: return@LazyColumn
                items(results) {
                    Button(
                        modifier = Modifier.fillMaxSize(),
                        onClick = {
                            viewModel.apply {
                                usernameUpdated(it.username)
                                passwordUpdated(it.username)
                                domainUpdated(it.domain)

                                killQrResults()

                                login()
                            }
                        }
                    ) {
                        Text(text = it.username)
                    }
                }
            }
        }
    )
}