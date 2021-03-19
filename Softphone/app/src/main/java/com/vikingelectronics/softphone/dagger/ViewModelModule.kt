package com.vikingelectronics.softphone.dagger

import com.vikingelectronics.softphone.networking.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent


@Module
@InstallIn(ViewModelComponent::class)
abstract class ActivityBindings {

    @Binds
    abstract fun bindActivityRepo(repo: ActivityRepositoryImpl): ActivityRepository

    @Binds
    abstract fun bindDeviceRepo(repo: DeviceRepositoryImpl): DeviceRepository

    @Binds
    abstract fun bindRecordsRepo(repo: RecordsRepositoryImpl): RecordsRepository
}