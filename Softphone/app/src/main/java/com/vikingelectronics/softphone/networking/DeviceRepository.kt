package com.vikingelectronics.softphone.networking

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.vikingelectronics.softphone.accounts.SipAccount
import com.vikingelectronics.softphone.accounts.User
import com.vikingelectronics.softphone.accounts.UserProvider
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.dagger.UserScope
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.extensions.timber
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.linphone.core.Call
import org.linphone.core.Core
import javax.inject.Inject
import kotlin.Exception

data class DeviceCall(val device: Device, val call: Call)

interface DeviceRepository {
    suspend fun getDevices(index: DocumentSnapshot?): FirebaseRepository.PaginationHolder<Device>
    fun getDeviceActivityList(device: Device): Flow<FirebaseRepository.ListState<ActivityEntry>>
    fun getDeviceForIncomingCall(): DeviceCall?
}

@UserScope
class DeviceRepositoryImpl @Inject constructor(
   override val db: FirebaseFirestore,
   override val storage: FirebaseStorage,
   override val user: User,
   override val sipAccount: SipAccount,
   val userProvider: UserProvider,
   val core: Core
): FirebaseRepository(), DeviceRepository {

    private suspend fun Device.getLatestDeviceActivity(): Device {
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

        return this
    }

    override suspend fun getDevices(index: DocumentSnapshot?): PaginationHolder<Device> {
        val list = mutableListOf<Device>()

        sipAccount.devices.iterateToObject<Device> {
            list.add(it.getLatestDeviceActivity())
        }

        userProvider.rebindSipAccountWithDevicesIfNecessary(user, sipAccount, list)

        return PaginationHolder(list, null)
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

    override fun getDeviceForIncomingCall(): DeviceCall? {
        val call = core.currentCall ?: return null
        val device = sipAccount.deviceObjects.find { it.callAddress == call.remoteAddress.asString() } ?: return null
        return DeviceCall(device, call)
    }
}