package com.vikingelectronics.softphone.dagger

import com.vikingelectronics.softphone.accounts.SipAccount
import com.vikingelectronics.softphone.accounts.User
import com.vikingelectronics.softphone.networking.*
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.hilt.DefineComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
annotation class UserScope

@UserScope
@DefineComponent(parent = SingletonComponent::class)
interface UserComponent {

    @DefineComponent.Builder
    interface Builder {
        fun setUser(@BindsInstance user: User): UserComponent.Builder
        fun setSip(@BindsInstance sipAccount: SipAccount): UserComponent.Builder
        fun build(): UserComponent
    }

    @Module()
    @InstallIn(UserComponent::class)
    interface UserBindings {
        @Binds
        abstract fun bindActivityRepo(repo: ActivityRepositoryImpl): ActivityRepository

        @Binds
        abstract fun bindDeviceRepo(repo: DeviceRepositoryImpl): DeviceRepository

        @Binds
        abstract fun bindRecordsRepo(repo: CapturesRepositoryImpl): CapturesRepository
    }
}

@InstallIn(UserComponent::class)
@EntryPoint
interface UserComponentEntryPoint {
    fun activityRepository(): ActivityRepository
    fun capturesRepository(): CapturesRepository
    fun deviceRepository(): DeviceRepository
}