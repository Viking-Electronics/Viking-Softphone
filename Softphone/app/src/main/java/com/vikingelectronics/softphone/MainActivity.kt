package com.vikingelectronics.softphone

import android.os.Bundle
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
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.vikingelectronics.softphone.accounts.UserProvider
import com.vikingelectronics.softphone.accounts.SipAccountDrawerHeader
import com.vikingelectronics.softphone.accounts.login.LoginScreen
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.activity.detail.ActivityDetail
import com.vikingelectronics.softphone.activity.list.ActivityList
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.captures.list.CapturesList
import com.vikingelectronics.softphone.databinding.FragmentContainerBinding
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.devices.detail.DeviceDetail
import com.vikingelectronics.softphone.devices.list.DevicesList
import com.vikingelectronics.softphone.extensions.getParcelableFromBackstack
import com.vikingelectronics.softphone.legacy.*
import com.vikingelectronics.softphone.legacy.schedules.ScheduleFragment
import com.vikingelectronics.softphone.legacy.schedules.ScheduleManager
import com.vikingelectronics.softphone.legacy.settings.*
import com.vikingelectronics.softphone.navigation.Screen
import com.vikingelectronics.softphone.util.LinphoneManager
import com.vikingelectronics.softphone.util.PermissionsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.linphone.core.*
import javax.inject.Inject

interface LegacyFragmentDependencyProvider {
    val core: Core
    val permissionsManager: PermissionsManager
    val linphonePreferences: LinphonePreferences
    val linphoneManager: LinphoneManager
    val navController: NavController
    val scheduleManager: ScheduleManager
}

@AndroidEntryPoint
class MainActivity: AppCompatActivity(), LegacyFragmentDependencyProvider {

    @Inject lateinit var userProvider: UserProvider

    @Inject override lateinit var core: Core
    @Inject override lateinit var linphoneManager: LinphoneManager
    @Inject override lateinit var linphonePreferences: LinphonePreferences
    @Inject override lateinit var permissionsManager: PermissionsManager
    @Inject override lateinit var scheduleManager: ScheduleManager
    override lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            userProvider.checkStoredSipCreds()

            withContext(Main) {
                setContent {
                    MaterialTheme {
                        navController = MainActivityComposable(supportFragmentManager, userProvider)
                    }
                }
            }
        }
    }
}


@Composable
fun MainActivityComposable(
    supportFragmentManager: FragmentManager,
    userProvider: UserProvider,
): NavController {
    var toolbarTitle by remember { mutableStateOf("") }
    var shouldShowToolbarActions = remember { mutableStateOf(false) }
    var toolbarActions: @Composable RowScope.() -> Unit by remember { mutableStateOf({}) }

    val scaffoldState = rememberScaffoldState()
    val navController = rememberNavController().apply {
        addOnDestinationChangedListener { _, _, _ ->
            shouldShowToolbarActions.value = false
        }
    }
    val scope = rememberCoroutineScope()

    val bottomNavItems = listOf(
        Screen.Primary.DeviceList,
        Screen.Primary.ActivityList,
        Screen.Primary.CaptureList
    )
    val drawerNavItems = listOf(
        Screen.Primary.Schedules,
        Screen.Primary.Info,
        Screen.Primary.Settings.Main
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.arguments?.getString(KEY_ROUTE)

    val isLoggedIn by userProvider.isLoggedIn.collectAsFlowState()

    Scaffold (
        scaffoldState = scaffoldState,
        topBar = {
            if (isLoggedIn)
             TopAppBar(
                 title = { Text(text = toolbarTitle) },
                 navigationIcon = {
                     Button(onClick = {
                         scope.launch {
                             scaffoldState.drawerState.apply {
                                 if (isOpen) close() else open()
                             }
                         }
                     }) {
                         Icon(
                             imageVector = Icons.Default.Menu,
                             contentDescription = "Drawer menu icon"
                         )
                     }
                 },
                 actions = if (shouldShowToolbarActions.value) toolbarActions else { {} }
             )
        },
        bottomBar = {
            if (isLoggedIn)
            BottomNavigation {
                bottomNavItems.forEach { screen ->
                    BottomNavigationItem(
                        icon = screen.icon,
                        label = { Text(text = stringResource(id = screen.displayResourceId)) },
                        selected = screen.route == currentRoute,
                        onClick = { navController.navigate(screen.route) })
                }
            }
        },
        drawerContent = {
            SipAccountDrawerHeader(userProvider)

            drawerNavItems.forEach { screen ->
                Divider()
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
                        text = stringResource(id = screen.displayResourceId),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        drawerGesturesEnabled = isLoggedIn
    ){
        val startDestination = if (isLoggedIn) Screen.Primary.DeviceList.route else Screen.Login.route
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(it)
        ) {

            composable(Screen.Login.route) {
                LoginScreen(navController)
            }

            composable(Screen.Primary.DeviceList.route) {
                toolbarTitle = stringResource(id = Screen.Primary.DeviceList.displayResourceId)
                DevicesList(navController = navController)
            }

            composable(Screen.Primary.ActivityList.route) {
                toolbarTitle = stringResource(id = Screen.Primary.ActivityList.displayResourceId)
                toolbarActions = ActivityList(navController = navController, shouldShowToolbarActions, scaffoldState.snackbarHostState)
            }

            composable(Screen.Primary.CaptureList.route) {
                toolbarTitle = stringResource(id = Screen.Primary.CaptureList.displayResourceId)
                toolbarActions = CapturesList(navController = navController, shouldShowToolbarActions, scaffoldState.snackbarHostState)
            }

            composable(Screen.Primary.Schedules.route) {
                toolbarTitle = stringResource(id = Screen.Primary.Schedules.displayResourceId)
                LegacyFragmentContainer(ScheduleFragment(), supportFragmentManager)
            }

            composable(Screen.Primary.Info.route) {
                toolbarTitle = stringResource(id = Screen.Primary.Info.displayResourceId)
                LegacyFragmentContainer(fragment = AboutFragment(), supportFragmentManager = supportFragmentManager)
            }

            composable(Screen.Primary.Settings.Main.route) {
                toolbarTitle = stringResource(id = Screen.Primary.Settings.Main.displayResourceId)
                LegacyFragmentContainer(fragment = SettingsFragment(), supportFragmentManager = supportFragmentManager)
            }

            composable(Screen.Primary.Settings.Tunnel.route) {
                toolbarTitle = stringResource(id = Screen.Primary.Settings.Tunnel.displayResourceId)
                LegacyFragmentContainer(fragment = TunnelSettingsFragment(), supportFragmentManager = supportFragmentManager)
            }

            composable(Screen.Primary.Settings.Audio.route) {
                toolbarTitle = stringResource(id = Screen.Primary.Settings.Audio.displayResourceId)
                LegacyFragmentContainer(fragment = AudioSettingsFragment(), supportFragmentManager = supportFragmentManager)
            }

            composable(Screen.Primary.Settings.Video.route) {
                toolbarTitle = stringResource(id = Screen.Primary.Settings.Video.displayResourceId)
                LegacyFragmentContainer(fragment = VideoSettingsFragment(), supportFragmentManager = supportFragmentManager)
            }

            composable(Screen.Primary.Settings.Call.route) {
                toolbarTitle = stringResource(id = Screen.Primary.Settings.Call.displayResourceId)
                LegacyFragmentContainer(fragment = CallSettingsFragment(), supportFragmentManager = supportFragmentManager)
            }

            composable(Screen.Primary.Settings.Network.route) {
                toolbarTitle = stringResource(id = Screen.Primary.Settings.Network.displayResourceId)
                LegacyFragmentContainer(fragment = NetworkSettingsFragment(), supportFragmentManager = supportFragmentManager)
            }

            composable(Screen.Primary.Settings.Advanced.route) {
                toolbarTitle = stringResource(id = Screen.Primary.Settings.Advanced.displayResourceId)
                LegacyFragmentContainer(fragment = AdvancedSettingsFragment(), supportFragmentManager = supportFragmentManager)
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
    return navController
}

@Composable
private fun LegacyFragmentContainer(fragment: Fragment, supportFragmentManager: FragmentManager) {
    AndroidViewBinding(FragmentContainerBinding::inflate) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}