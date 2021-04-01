package com.vikingelectronics.softphone.extensions

fun Boolean?.toInt(): Int = if (this != null && this) 1 else 0