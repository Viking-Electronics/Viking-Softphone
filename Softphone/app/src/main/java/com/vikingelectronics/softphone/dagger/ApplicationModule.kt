package com.vikingelectronics.softphone.dagger

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
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
    }

    @Singleton
    @Provides
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore.apply {
        firestoreSettings { isPersistenceEnabled = true }
    }
}