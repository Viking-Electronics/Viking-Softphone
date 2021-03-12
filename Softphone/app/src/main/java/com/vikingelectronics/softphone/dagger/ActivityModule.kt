package com.vikingelectronics.softphone.dagger

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.vikingelectronics.softphone.MainActivity
import com.vikingelectronics.softphone.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {

    @Provides
    @ViewModelScoped
    fun provideNavController(activity: MainActivity): NavController = activity.findNavController(R.id.fragmentContainer)

//    fun provideDexter(activity: AppCompatActivity): Dexter {
//        Dexter.withContext()
//    }
}