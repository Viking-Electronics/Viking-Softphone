package com.vikingelectronics.softphone

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

//Link to explanation of crazy amount of "Accessing hidden field/method" logs
//https://stackoverflow.com/questions/64948927/using-android-11-gives-a-lot-of-output-log-about-accessing-sqlitedatabase-interf

@HiltAndroidApp
class SoftphoneApplication: MultiDexApplication() {
}