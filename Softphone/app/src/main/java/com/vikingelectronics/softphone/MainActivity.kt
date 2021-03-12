package com.vikingelectronics.softphone

import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.ViewParent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.vikingelectronics.softphone.call.CallFragment
import com.vikingelectronics.softphone.call.CallVideoFragment
import com.vikingelectronics.softphone.call.IncomingCallReceiver
import com.vikingelectronics.softphone.databinding.ActivityMainBinding
import com.vikingelectronics.softphone.navigation.ContentHostFragment
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

    private val binding: ActivityMainBinding by viewBinding()

    private lateinit var callReceiver: IncomingCallReceiver

    private val coreListener = object: CoreListenerStub() {
        override fun onCallStateChanged(lc: Core, call: Call, cstate: Call.State, message: String) {
            super.onCallStateChanged(lc, call, cstate, message)
            Log.d("Call state changed", "$call, $cstate, $message")
            when(cstate) {
                Call.State.Connected, Call.State.StreamsRunning -> binding.mainPager.currentItem = 1
                Call.State.IncomingReceived -> call.accept()
                else -> binding.mainPager.currentItem = 0
            }
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

        core.addListener(coreListener)
        setupPager()
    }

    private fun setupPager() = with(binding.mainPager) {
        isUserInputEnabled = false
        adapter = fragmentStateAdapter
//        offscreenPageLimit = 0
        orientation = ViewPager2.ORIENTATION_HORIZONTAL
    }

    private fun setupIntentFilter() {
        val filter = IntentFilter().apply {

        }
    }
}