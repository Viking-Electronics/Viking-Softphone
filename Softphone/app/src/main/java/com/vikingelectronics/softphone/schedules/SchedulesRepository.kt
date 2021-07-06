package com.vikingelectronics.softphone.schedules

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vikingelectronics.shared.accounts.SipAccount
import com.vikingelectronics.shared.accounts.User
import com.vikingelectronics.softphone.dagger.UserScope
import com.vikingelectronics.softphone.extensions.emitUnitResult
import com.vikingelectronics.softphone.extensions.unitResult
import com.vikingelectronics.softphone.networking.FirebaseRepository
import com.vikingelectronics.shared.schedules.Schedule
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface SchedulesRepository {
    suspend fun fetchSchedules(index: DocumentSnapshot?): FirebaseRepository.PaginationHolder<Schedule>
    suspend fun addNew(schedule: Schedule): Result<Unit>
    suspend fun updateSchedule(updatedSchedule: Schedule): Result<Unit>
    suspend fun delete(schedules: List<Schedule>): Flow<Result<Unit>>
}

@UserScope
class SchedulesRepositoryImpl @Inject constructor(
    override val db: FirebaseFirestore,
    override val sipAccount: SipAccount,
    override val storage: FirebaseStorage,
    override val user: User
): FirebaseRepository(), SchedulesRepository {

    override suspend fun fetchSchedules(index: DocumentSnapshot?): PaginationHolder<Schedule> {
        val list = mutableListOf<Schedule>()

        schedulesCollectionRef.get().await().documents.iterateToObject<Schedule> {
            list.add(it)
        }

        return PaginationHolder(list, null)
    }

    override suspend fun addNew(schedule: Schedule): Result<Unit> {
        return schedulesCollectionRef.add(schedule).unitResult()
    }

    override suspend fun updateSchedule(updatedSchedule: Schedule): Result<Unit> {
        return schedulesCollectionRef
            .document(updatedSchedule.id)
            .set(updatedSchedule)
            .unitResult()
    }

    override suspend fun delete(schedules: List<Schedule>): Flow<Result<Unit>> = flow {
        schedules.forEach { schedule ->
            //There's some funky unwrapping happening if the value of .unitResult
            // is emitted after captured in a val, this seems to be an acceptable work around.
            schedulesCollectionRef.document(schedule.id).delete().emitUnitResult()
        }
    }
}