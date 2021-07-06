package com.vikingelectronics.shared

inline fun <T: Any, R: Any, Q: Any> initOrNull(first: T?, second: R?, invocation: (T, R) -> Q?): Q? {
    return if (first != null && second != null) invocation(first, second) else null
}