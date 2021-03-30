package com.vikingelectronics.softphone.networking

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.devices.Device
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface DeviceRepository {
    fun getDevices(username: String): Flow<Device>
    fun getDeviceActivityList(device: Device): Flow<ActivityEntry>
}

@ViewModelScoped
class DeviceRepositoryImpl @Inject constructor(
   override val db: FirebaseFirestore,
   override val storage: FirebaseStorage
): FirebaseRepository(), DeviceRepository {

    private suspend fun Device.getLatestDeviceActivity() {
        val deviceRef = devicesCollectionRef.document(id)

        activityCollectionRef.whereEqualTo("sourceDevice", deviceRef)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .getAwait()
            .documents[0]
            .toObject<ActivityEntry>()
            ?.let { entry ->
                latestActivityEntry = entry
            }
    }

    override fun getDevices(username: String): Flow<Device> = flow {
        val user = getUser(username) ?: return@flow
        val sipAccount = getSipAccount(user)

        sipAccount?.devices?.iterateToObject<Device> { device ->
            device.getLatestDeviceActivity()
            emit(device)
        }
    }

    override fun getDeviceActivityList(device: Device): Flow<ActivityEntry> = flow {
        val deviceDocRef = devicesCollectionRef.document(device.id)

        activityCollectionRef.whereEqualTo("sourceDevice", deviceDocRef)
            .getAwait()
            .documents
            .apply {
                subList(1, size).iterateToObject<ActivityEntry> { emit(it) }
            }

    }
}