package com.vikingelectronics.softphone.networking

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.vikingelectronics.softphone.accounts.SipAccount
import com.vikingelectronics.softphone.accounts.User
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.dagger.UserScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject



interface ActivityRepository {
    fun getAllEntries(): Flow<ActivityEntry>
    suspend fun fetchEntries(lastEntry: DocumentSnapshot? = null): FirebaseRepository.PaginationHolder<ActivityEntry>
}

@UserScope
class ActivityRepositoryImpl @Inject constructor(
    override val db: FirebaseFirestore,
    override val storage: FirebaseStorage,
    override val user: User,
    override val sipAccount: SipAccount
): FirebaseRepository(), ActivityRepository {

    override suspend fun fetchEntries(lastEntry: DocumentSnapshot?): PaginationHolder<ActivityEntry> {
        var query = activityCollectionRef.whereIn("sourceDevice", sipAccount.devices)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(25)

        lastEntry?.let {
            query = query.startAfter(it)
        }

        val docs = query.getAwait().documents

        return PaginationHolder(docs.iterateToObjectList(), docs.last())
    }

    override fun getAllEntries(): Flow<ActivityEntry> = flow {
        sipAccount.devices.forEach { ref ->
            activityCollectionRef.whereEqualTo("sourceDevice", ref)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .iterateToObject<ActivityEntry> { emit(it) }
        }

    }

    suspend fun generateEntries()  {
        val storageRefs = Firebase.storage.reference.listAll().await().items
        val devicesRef = devicesCollectionRef.get().await().documents

        storageRefs.forEachIndexed { index, item ->
            val deviceDoc = if (index % 2 == 0) devicesRef[0] else devicesRef[1]
            val deviceName = deviceDoc.get("name").toString()
            val downloadUrl = item.downloadUrl.await().toString()
            val entry = hashMapOf(
                "description" to "Generated by index: $index",
                "sourceName" to deviceName,
                "sourceDevice" to deviceDoc.reference,
                "snapshotUrl" to downloadUrl,
                "timestamp" to FieldValue.serverTimestamp()
            )

            activityCollectionRef.add(entry).addOnSuccessListener {
                Timber.d("Entry added: ${it.id}")
            }
        }
    }
}