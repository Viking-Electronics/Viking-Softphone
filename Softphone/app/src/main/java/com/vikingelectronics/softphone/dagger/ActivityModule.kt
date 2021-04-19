package com.vikingelectronics.softphone.dagger

import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.vikingelectronics.softphone.MainActivity
import com.vikingelectronics.softphone.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ActivityComponent::class)
object ActivityProviders {

//    @Provides
//    @ViewModelScoped
//    fun provideNavController(activity: MainActivity): NavController = activity.findNavController(R.id.fragmentContainer)

}