package com.vikingelectronics.softphone.dagger

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.pandulapeter.beagle.Beagle
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tfcporciuncula.flow.FlowSharedPreferences
import com.vikingelectronics.softphone.accounts.RepositoryProvider
import com.vikingelectronics.softphone.accounts.UserProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.linphone.core.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Singleton
    @Provides
    fun provideLinphoneFactory(): Factory = Factory.instance().apply {
        this.loggingService.addListener { loggingService, tag, logLevel, message ->
            Beagle.log(message, tag)
        }
        setDebugMode(true, "LinphoneDebug")
    }

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
        presenceModel = createPresenceModel().apply { basicStatus = PresenceBasicStatus.Open }
        val deviceName: String = "Test"
        val appName: String = "VikingSoftphone"
        val androidVersion = BuildConfig.VERSION_NAME
        val userAgent = "$appName/$androidVersion ($deviceName) LinphoneSDK"

        setUserAgent(
            userAgent,
            "${context.getString(R.string.linphone_sdk_version)} (${context.getString(R.string.linphone_sdk_branch)})"
        )
        isAutoIterateEnabled = true
        start()
    }

    @Singleton
    @Provides
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore.apply {
        firestoreSettings { isPersistenceEnabled = true }
    }

    @Singleton
    @Provides
    fun provideStorage(): FirebaseStorage = Firebase.storage

    @Singleton
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @Singleton
    @Provides
    fun provideFSP(@ApplicationContext context: Context): FlowSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val preferences = EncryptedSharedPreferences.create(
            context,
            "v_softphone_prefs_5.xml",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return FlowSharedPreferences(preferences)
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface ApplicationBindings {
    @Binds
    @Singleton
    fun bindRepositoryProvider(userProvider: UserProvider): RepositoryProvider
}