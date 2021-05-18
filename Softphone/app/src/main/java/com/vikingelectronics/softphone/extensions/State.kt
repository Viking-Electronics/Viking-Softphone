package com.vikingelectronics.softphone.extensions

import androidx.compose.runtime.MutableState

fun MutableState<Boolean>.invert() {
    value = !value
}