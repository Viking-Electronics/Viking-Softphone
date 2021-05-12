package com.vikingelectronics.softphone.networking

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.vikingelectronics.softphone.accounts.SipAccount
import com.vikingelectronics.softphone.accounts.User
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.dagger.UserScope
import com.vikingelectronics.softphone.devices.Device
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.Exception

interface DeviceRepository {
    fun getDevices(username: String): Flow<Device>
    fun getDeviceActivityList(device: Device): Flow<FirebaseRepository.ListState<ActivityEntry>>
}

@UserScope
class DeviceRepositoryImpl @Inject constructor(
   override val db: FirebaseFirestore,
   override val storage: FirebaseStorage,
   override val user: User,
   override val sipAccount: SipAccount
): FirebaseRepository(), DeviceRepository {

    private suspend fun Device.getLatestDeviceActivity() {
        val deviceRef = devicesCollectionRef.document(id)

        val entries = activityCollectionRef.whereEqualTo("sourceDevice", deviceRef)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .getAwait()

            latestActivityEntry = try {
                entries.documents[0].toObject<ActivityEntry>()
            } catch (e: Exception) {
                null
            }
    }

    override fun getDevices(username: String): Flow<Device> = flow {
        sipAccount.devices.iterateToObject<Device> { device ->
            device.getLatestDeviceActivity()
            emit(device)
        }
    }

    override fun getDeviceActivityList(device: Device): Flow<ListState<ActivityEntry>> = flow {
        emit(ListState.Loading)

        val deviceDocRef = devicesCollectionRef.document(device.id)

        try {
            activityCollectionRef.whereEqualTo("sourceDevice", deviceDocRef)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .getAwait()
                .documents
                .apply {
                    subList(1, size).iterateActorToObjectList<ActivityEntry> { emit(ListState.Success(it)) }
                }
        } catch (e: Exception) {
            emit(ListState.Failure(e))
        }
    }
}