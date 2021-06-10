package com.vikingelectronics.softphone.extensions

fun <T> MutableList<T>.addIfAbsentElseRemove(element: T)
    = if (contains(element)) remove(element) else add(element)
