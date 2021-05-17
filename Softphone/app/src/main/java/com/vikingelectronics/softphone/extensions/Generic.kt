package com.vikingelectronics.softphone.extensions

import timber.log.Timber

inline fun <T> T.timber(prefix: String = "", postfix: String = ""): T = this.apply {
    Timber.d("$prefix ${toString()} $postfix")
}

inline fun <T: Any?> T?.initIfNull(initializer: () -> T): T {
    return this ?: initializer()
}

inline fun <T: Any, R: Any, Q: Any> initOrNull(first: T?, second: R?, invocation: (T, R) -> Q?): Q? {
    return if (first != null && second != null) invocation(first, second) else null
}

inline fun <T: Any?, R: Any?> invokeIfNotNull(first: T?, second: R?, invocation: (T, R) -> Unit ) {
    if (first != null && second != null) invocation.invoke(first, second)
}