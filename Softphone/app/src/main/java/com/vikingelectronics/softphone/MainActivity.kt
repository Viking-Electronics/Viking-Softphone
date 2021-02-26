package com.vikingelectronics.softphone

import android.content.IntentFilter
import android.os.Bundle
import android.view.ViewParent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.vikingelectronics.softphone.call.CallFragment
import com.vikingelectronics.softphone.call.CallVideoFragment
import com.vikingelectronics.softphone.call.IncomingCallReceiver
import com.vikingelectronics.softphone.databinding.ActivityMainBinding
import com.vikingelectronics.softphone.navigation.ContentHostFragment
import dagger.hilt.android.AndroidEntryPoint
import org.linphone.core.Core
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: AppCompatActivity(R.layout.activity_main) {

    @Inject lateinit var core: Core

    private val binding: ActivityMainBinding by viewBinding()

    private lateinit var callReceiver: IncomingCallReceiver

    private val fragmentStateAdapter = object : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment = when(position) {
            0 -> ContentHostFragment()
            else -> CallVideoFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupPager()
//        binding.mainPager.currentItem = 1
    }

    private fun setupPager() = with(binding.mainPager) {
        isUserInputEnabled = false
        adapter = fragmentStateAdapter
        offscreenPageLimit = 1
        orientation = ViewPager2.ORIENTATION_HORIZONTAL
    }

    private fun setupIntentFilter() {
        val filter = IntentFilter().apply {

        }
    }
}