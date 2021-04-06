package com.vikingelectronics.softphone.navigation

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isGone
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.databinding.FragmentHostBinding
import dagger.hilt.android.AndroidEntryPoint
import org.linphone.core.Core
import javax.inject.Inject

@AndroidEntryPoint
class ContentHostFragment: Fragment(R.layout.fragment_host) {

//    private val viewModel: ContentHostViewModel by viewModels()
    private val binding: FragmentHostBinding by viewBinding()
//    private val backButtonCallback = object: OnBackPressedCallback(true) {
//        override fun handleOnBackPressed() {
////            navController.navigateUp()
//        }
//    }

    @Inject lateinit var core: Core

//    lateinit var appBarConfig: AppBarConfiguration
//    lateinit var navController: NavController

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
//        setupNav()
//        checkActiveAccount()
    }


//    private fun setupNav() = with(binding){
//        navController = fragmentContainer.findNavController()
//        appBarConfig = AppBarConfiguration(
//            setOf(
//                R.id.deviceListFragment,
//                R.id.activityListFragment,
//                R.id.capturesListFragment
//            ),
//            drawerLayout
//        )
//
//        toolbar.setupWithNavController(navController, appBarConfig)
//        navView.setupWithNavController(navController)
//        bottomNavBar.setupWithNavController(navController)
//
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backButtonCallback)
//        headerLayout.viewModel = this@ContentHostFragment.viewModel
//    }

//    private fun checkActiveAccount() = with(core) {
//        if (defaultProxyConfig == null || defaultProxyConfig?.identityAddress == null) {
//            binding.toolbar.isGone = true
//            binding.bottomNavBar.isGone = true
//            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
//            navController.navigate(R.id.accounts_graph)
//        }
//    }
}
