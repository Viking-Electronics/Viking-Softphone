package com.vikingelectronics.softphone

import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.activity.detail.ActivityDetail
import com.vikingelectronics.softphone.activity.list.ActivityList
import com.vikingelectronics.softphone.call.CallVideoFragment
import com.vikingelectronics.softphone.call.IncomingCallReceiver
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.captures.list.CapturesList
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.devices.detail.DeviceDetail
import com.vikingelectronics.softphone.devices.list.DevicesList
import com.vikingelectronics.softphone.extensions.getParcelableFromBackstack
import com.vikingelectronics.softphone.navigation.Screen
import com.vikingelectronics.softphone.util.LinphoneManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {

    @Inject lateinit var core: Core
    @Inject lateinit var linphoneManager: LinphoneManager

    private lateinit var callReceiver: IncomingCallReceiver

    private val coreListener = object: CoreListenerStub() {
        override fun onCallStateChanged(lc: Core, call: Call, cstate: Call.State, message: String) {
            super.onCallStateChanged(lc, call, cstate, message)
            Log.d("Call state changed", "$call, $cstate, $message")
//            when(cstate) {
//                Call.State.Connected, Call.State.StreamsRunning -> binding.mainPager.currentItem = 1
//                Call.State.IncomingReceived -> call.accept()
//                else -> binding.mainPager.currentItem = 0
//            }
        }
    }

//    private val fragmentStateAdapter = object : FragmentStateAdapter(this) {
//        override fun getItemCount(): Int = 2
//
//        override fun createFragment(position: Int): Fragment = when(position) {
//            0 -> ContentHostFragment()
//            else -> CallVideoFragment()
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MainActivityComposable()
            }
        }

        core.addListener(coreListener)
    }


    private fun setupIntentFilter() {
        val filter = IntentFilter().apply {

        }
    }
}


@Composable
fun MainActivityComposable() {

    val scaffoldState = rememberScaffoldState()
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val bottomNavItems = listOf(
        Screen.Primary.DeviceList,
        Screen.Primary.ActivityList,
        Screen.Primary.CaptureList
    )
    val drawerNavItems = listOf(
        Screen.Primary.Schedules,
        Screen.Primary.Info,
        Screen.Primary.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.arguments?.getString(KEY_ROUTE)

    var toolbarTitle by remember { mutableStateOf("") }
    var toolbarActions: @Composable RowScope.() -> Unit by remember { mutableStateOf({}) }

    Scaffold (
        scaffoldState = scaffoldState,
        topBar = {
             TopAppBar(
                 title = { Text(text = toolbarTitle) },
                 navigationIcon = {
                     Icon(
                         imageVector = Icons.Default.Menu,
                         contentDescription = "Drawer menu icon",
                         modifier = Modifier.clickable {
                             scope.launch {
                                 scaffoldState.drawerState.apply {
                                     if (isOpen) close() else open()
                                 }
                             }
                         }
                     )
                 },
                 actions = toolbarActions
             )
        },
        bottomBar = {
            BottomNavigation {
                bottomNavItems.forEach { screen ->
                    BottomNavigationItem(
                        icon = screen.icon,
                        label = { Text(text = stringResource(id = screen.toolbarResourceId)) },
                        selected = screen.route == currentRoute,
                        onClick = { navController.navigate(screen.route) })
                }
            }
        },
        drawerContent = {
            drawerNavItems.forEach { screen ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(start = 8.dp)
                        .clickable {
                            scope.launch {
                                scaffoldState.drawerState.close()
                            }
                            navController.navigate(screen.route)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    screen.icon()
                    Text(
                        text = stringResource(id = screen.toolbarResourceId),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Divider()
            }
        }
    ){
        NavHost(navController = navController, startDestination = Screen.Primary.DeviceList.route) {
            composable(Screen.Primary.DeviceList.route) {
                toolbarTitle = stringResource(id = Screen.Primary.DeviceList.toolbarResourceId)
                DevicesList(navController = navController)
            }

            composable(Screen.Primary.ActivityList.route) {
                toolbarTitle = stringResource(id = Screen.Primary.ActivityList.toolbarResourceId)
                ActivityList(navController = navController)
            }

            composable(Screen.Primary.CaptureList.route) {
                toolbarTitle = stringResource(id = Screen.Primary.CaptureList.toolbarResourceId)
                CapturesList(navController = navController)
            }

            composable(Screen.Primary.Schedules.route) {

            }

            composable(Screen.Primary.Info.route) {

            }

            composable(Screen.Primary.Settings.route) {

            }

            composable(
                Screen.Secondary.DeviceDetail.route,
                arguments = listOf(navArgument("device") { type = NavType.ParcelableType(Device::class.java) })
            ) {
                val device: Device = navController.getParcelableFromBackstack(Screen.Secondary.DeviceDetail) ?: return@composable
                toolbarTitle = device.name
                DeviceDetail(device = device, navController = navController)
            }

            composable(
                Screen.Secondary.ActivityDetail.route,
                arguments = listOf(navArgument("activity") { type = NavType.ParcelableType(ActivityEntry::class.java) })
            ) {
                val activity: ActivityEntry = navController.getParcelableFromBackstack(Screen.Secondary.ActivityDetail) ?: return@composable
                toolbarTitle = activity.sourceName
                ActivityDetail(entry = activity)
            }

            composable(
                Screen.Secondary.CaptureDetail.route,
                arguments = listOf(navArgument("device") { type = NavType.ParcelableType(Device::class.java) })
            ) {
                val capture: Capture = navController.getParcelableFromBackstack(Screen.Secondary.CaptureDetail) ?: return@composable

            }
        }
    }
}