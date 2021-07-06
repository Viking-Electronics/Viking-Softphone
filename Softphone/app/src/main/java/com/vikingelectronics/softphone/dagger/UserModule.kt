package com.vikingelectronics.softphone.dagger

import com.vikingelectronics.shared.accounts.SipAccount
import com.vikingelectronics.shared.accounts.User
import com.vikingelectronics.shared.activity.ActivityRepository
import com.vikingelectronics.shared.activity.ActivityRepositoryImpl
import com.vikingelectronics.shared.captures.CapturesRepository
import com.vikingelectronics.shared.captures.CapturesRepositoryImpl
import com.vikingelectronics.softphone.networking.*
import com.vikingelectronics.softphone.schedules.SchedulesRepository
import com.vikingelectronics.softphone.schedules.SchedulesRepositoryImpl
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
        fun setUser(@BindsInstance user: User): Builder
        fun setSip(@BindsInstance sipAccount: SipAccount): Builder
        fun build(): UserComponent
    }

    @Module()
    @InstallIn(UserComponent::class)
    interface UserBindings {
        @Binds
        fun bindActivityRepo(repo: ActivityRepositoryImpl): ActivityRepository

        @Binds
        fun bindDeviceRepo(repo: DeviceRepositoryImpl): DeviceRepository

        @Binds
        fun bindRecordsRepo(repo: CapturesRepositoryImpl): CapturesRepository

        @Binds
        fun bindSchedulesRepo(repo: SchedulesRepositoryImpl): SchedulesRepository
    }
}

@InstallIn(UserComponent::class)
@EntryPoint
interface UserComponentEntryPoint {
    fun activityRepository(): ActivityRepository
    fun capturesRepository(): CapturesRepository
    fun deviceRepository(): DeviceRepository
    fun schedulesRepository(): SchedulesRepository
    fun user(): User
}