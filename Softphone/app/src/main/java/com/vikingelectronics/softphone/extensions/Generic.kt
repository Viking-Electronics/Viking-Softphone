package com.vikingelectronics.softphone.extensions

import timber.log.Timber

fun <T> T.timber(prefix: String = "", postfix: String = ""): T = this.apply {
    Timber.d("$prefix ${toString()} $postfix")
}