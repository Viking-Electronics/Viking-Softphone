package com.vikingelectronics.softphone.util.extensions

inline fun <T: Any?> T?.initIfNull(initializer: () -> T): T {
    return this ?: initializer.invoke()
}

inline fun <T: Any?, R: Any?> invokeIfNotNull(first: T?, second: R?, invocation: (T, R) -> Unit ) {
    if (first != null && second != null) invocation.invoke(first, second)
}