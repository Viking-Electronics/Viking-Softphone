package com.vikingelectronics.softphone

import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.ViewParent
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.vikingelectronics.softphone.activity.list.ActivityListScreen
import com.vikingelectronics.softphone.call.CallFragment
import com.vikingelectronics.softphone.call.CallVideoFragment
import com.vikingelectronics.softphone.call.IncomingCallReceiver
import com.vikingelectronics.softphone.captures.list.CapturesListScreen
import com.vikingelectronics.softphone.databinding.ActivityMainBinding
import com.vikingelectronics.softphone.devices.list.DeviceListViewModel
import com.vikingelectronics.softphone.devices.list.DevicesListScreen
import com.vikingelectronics.softphone.navigation.ContentHostFragment
import com.vikingelectronics.softphone.navigation.Screen
import com.vikingelectronics.softphone.util.LinphoneManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: AppCompatActivity(R.layout.activity_main) {

    @Inject lateinit var core: Core
    @Inject lateinit var linphoneManager: LinphoneManager

//    private val binding: ActivityMainBinding by viewBinding()

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

    private val fragmentStateAdapter = object : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment = when(position) {
            0 -> ContentHostFragment()
            else -> CallVideoFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MainActivityComposable()
            }
        }

        core.addListener(coreListener)
//        setupPager()
    }

//    private fun setupPager() = with(binding.mainPager) {
//        isUserInputEnabled = false
//        adapter = fragmentStateAdapter
////        offscreenPageLimit = 0
//        orientation = ViewPager2.ORIENTATION_HORIZONTAL
//    }

    private fun setupIntentFilter() {
        val filter = IntentFilter().apply {

        }
    }
}


@Composable
fun MainActivityComposable() {
    
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        Screen.TopLevel.DeviceList,
        Screen.TopLevel.ActivityList,
        Screen.TopLevel.CaptureList
    )
    Scaffold (
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.arguments?.getString(KEY_ROUTE)

                bottomNavItems.forEach { screen ->
                    BottomNavigationItem(
                        icon = screen.icon,
                        label = { Text(text = stringResource(id = screen.toolbarResourceId)) },
                        selected = screen.route == currentRoute,
                        onClick = { navController.navigate(screen.route) })
                }

            }
        }
    ){
        NavHost(navController = navController, startDestination = Screen.TopLevel.DeviceList.route) {
            composable(Screen.TopLevel.DeviceList.route) { DevicesListScreen(navController = navController) }

            composable(Screen.TopLevel.ActivityList.route) { ActivityListScreen(navController = navController) }

            composable(Screen.TopLevel.CaptureList.route) { CapturesListScreen(navController = navController) }
        }
    }
}