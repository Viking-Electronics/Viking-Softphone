package com.vikingelectronics.shared

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}