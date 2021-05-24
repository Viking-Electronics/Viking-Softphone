package com.vikingelectronics.softphone.extensions

import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.*
import com.vikingelectronics.softphone.navigation.Screen

fun NavController.setParcelableAndNavigate(screen: Screen.Secondary, obj: Parcelable, optionsBuilder: (NavOptionsBuilder.() -> Unit)? = null) {
    currentBackStackEntry?.arguments?.apply {
        putParcelable(screen.parcelableKey, obj)
    } ?: kotlin.run {
        currentBackStackEntry?.arguments = Bundle().apply {
            putParcelable(screen.parcelableKey, obj)
        }
    }
    val options = optionsBuilder?.let { navOptions(it) }
    navigate(screen.route, navOptions = options)
}

fun <T: Parcelable> NavController.getParcelableFromBackstack(screen: Screen.Secondary): T? = previousBackStackEntry?.arguments?.getParcelable(screen.parcelableKey)