package com.vikingelectronics.softphone.dagger

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tfcporciuncula.flow.FlowSharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import org.linphone.core.Config
import org.linphone.core.Core
import org.linphone.core.Factory
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
            "v_softphone_prefs_2.xml",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return FlowSharedPreferences(preferences)
    }
}