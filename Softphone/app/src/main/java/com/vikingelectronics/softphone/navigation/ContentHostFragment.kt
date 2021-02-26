package com.vikingelectronics.softphone.navigation

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isGone
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.vikingelectronics.softphone.MainActivity
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.databinding.FragmentHostBinding
import com.vikingelectronics.softphone.databinding.ViewNavHeaderBinding
import dagger.hilt.android.AndroidEntryPoint
import org.linphone.core.Core
import javax.inject.Inject

@AndroidEntryPoint
class ContentHostFragment: Fragment(R.layout.fragment_host) {

    private val viewModel: ContentHostViewModel by viewModels()
    private val binding: FragmentHostBinding by viewBinding()
    private val backButtonCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigateUp()
        }
    }

    @Inject lateinit var core: Core

    lateinit var appBarConfig: AppBarConfiguration
    lateinit var navController: NavController

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setupNav()
        checkActiveAccount()
    }


    private fun setupNav() = with(binding){
        navController = fragmentContainer.findNavController()
        appBarConfig = AppBarConfiguration(
            setOf(
                R.id.devicesFragment,
                R.id.activityFragment,
                R.id.schedulesFragment
            ),
            drawerLayout
        )

        toolbar.setupWithNavController(navController, appBarConfig)
        navView.setupWithNavController(navController)
        bottomNavBar.setupWithNavController(navController)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backButtonCallback)
        headerLayout.viewModel = this@ContentHostFragment.viewModel
    }

    private fun checkActiveAccount() = with(core) {
        if (defaultProxyConfig == null || defaultProxyConfig.identityAddress == null) {
            binding.toolbar.isGone = true
            binding.bottomNavBar.isGone = true
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            navController.navigate(R.id.accounts_graph)
        }
    }
}
