package com.vikingelectronics.softphone.dagger

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import org.linphone.core.Config
import org.linphone.core.Core
import org.linphone.core.Factory
import org.linphone.utils.LinphoneUtils
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Singleton
    @Provides
    fun provideLinphoneFactory(): Factory = Factory.instance()

    @Singleton
    @Provides
    fun provideLinphoneConfig(@ApplicationContext context: Context, factory: Factory): Config {
        val base = context.filesDir.absolutePath
        val configFile = "$base/.linphonerc"
        val configFactoryFile = "$base/linphonerc"

        return factory.createConfigWithFactory(configFile, configFactoryFile)
    }

    @Singleton
    @Provides
    fun provideLinphoneCore(
        @ApplicationContext context: Context,
        factory: Factory,
        config: Config
    ): Core = factory.createCoreWithConfig(config, context).apply {
        start()

        //apparently linphone requires the client to explicitly call iterate or nothing will work
        GlobalScope.launch(Dispatchers.Default) {
            while (this@apply != null) {
                delay(30)
                withContext(Dispatchers.Main) {
                    iterate()
                }
            }
        }
    }
}