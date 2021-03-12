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

    private val applicationScope = CoroutineScope(SupervisorJob())

    @Inject
    lateinit var core: Core

    override fun onCreate() {
        super.onCreate()
        iterateCore()
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

    /**
     *Previously I had the iteration occurring within the ApplicationModule.
     * I moved it here as the logs get VERY obscure when crashes occur related to linphone
     */
    private fun iterateCore() = with(core) {
        //apparently linphone requires the client to explicitly call iterate or nothing will work
        applicationScope.launch(Dispatchers.Default) {
            while (this@with != null) {
                delay(30)
                withContext(Dispatchers.Main) {
                    iterate()
                }
            }
        }
    }
}