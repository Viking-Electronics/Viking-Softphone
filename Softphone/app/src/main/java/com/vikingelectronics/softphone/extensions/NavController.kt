package com.vikingelectronics.softphone.extensions

import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavController
import com.vikingelectronics.softphone.navigation.Screen

fun NavController.setParcelableAndNavigate(screen: Screen.Secondary, obj: Parcelable) {
    currentBackStackEntry?.arguments?.apply {
        putParcelable(screen.parcelableKey, obj)
    } ?: kotlin.run {
        currentBackStackEntry?.arguments = Bundle().apply {
            putParcelable(screen.parcelableKey, obj)
        }
    }
    navigate(screen.route)
}

fun <T: Parcelable> NavController.getParcelableFromBackstack(screen: Screen.Secondary): T? = previousBackStackEntry?.arguments?.getParcelable(screen.parcelableKey)