package com.vikingelectronics.softphone.util

import org.linphone.core.Address
import org.linphone.core.Core

fun Core.restart() {
    stop()
    start()
}

fun Address.getSafeDisplayName(): String {
    return when {
        displayName != null && displayName.isNotEmpty() -> displayName
        username != null && username.isNotEmpty() -> username
        else -> asStringUriOnly()
    }
}