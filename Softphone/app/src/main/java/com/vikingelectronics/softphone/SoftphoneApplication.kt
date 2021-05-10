package com.vikingelectronics.softphone

import androidx.multidex.MultiDexApplication
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.CoilUtils
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Behavior
import com.pandulapeter.beagle.log.BeagleLogger
import com.pandulapeter.beagle.modules.*
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
        initBeagle()
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

    private fun initBeagle() {
        Beagle.initialize(
            this,
            behavior = Behavior(
                logBehavior = Behavior.LogBehavior(
                    loggers = listOf(BeagleLogger)
                )
            )
        )
        Beagle.set(
            HeaderModule(
                title = getString(R.string.app_name),
                subtitle = BuildConfig.APPLICATION_ID,
                text = "${BuildConfig.BUILD_TYPE} v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            ),
            AppInfoButtonModule(),
            DeveloperOptionsButtonModule(),
            PaddingModule(),
            TextModule("General", TextModule.Type.SECTION_HEADER),
            KeylineOverlaySwitchModule(),
            AnimationDurationSwitchModule(),
            ScreenCaptureToolboxModule(),
            DividerModule(),
            TextModule("Logs", TextModule.Type.SECTION_HEADER),
            NetworkLogListModule(), // Might require additional setup, see below
            LogListModule(), // Might require additional setup, see below
            LifecycleLogListModule(),
            DividerModule(),
            TextModule("Other", TextModule.Type.SECTION_HEADER),
            DeviceInfoModule(),
            BugReportButtonModule()
        )
    }
}