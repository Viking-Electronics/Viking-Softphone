package com.vikingelectronics.softphone

import androidx.multidex.MultiDexApplication
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.CoilUtils
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import org.linphone.core.Core
import timber.log.Timber
import javax.inject.Inject

//Link to explanation of crazy amount of "Accessing hidden field/method" logs
//https://stackoverflow.com/questions/64948927/using-android-11-gives-a-lot-of-output-log-about-accessing-sqlitedatabase-interf

@HiltAndroidApp
class SoftphoneApplication: MultiDexApplication(), ImageLoaderFactory {


    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this).apply {
            crossfade(true)
            okHttpClient {
                OkHttpClient.Builder().apply {
                    cache(CoilUtils.createDefaultCache(this@SoftphoneApplication))
                }.build()
            }
        }.build()
    }
}