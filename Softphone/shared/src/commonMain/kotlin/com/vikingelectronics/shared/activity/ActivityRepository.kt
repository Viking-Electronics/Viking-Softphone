package com.vikingelectronics.shared.activity



import com.vikingelectronics.shared.accounts.SipAccount
import com.vikingelectronics.shared.accounts.User
import com.vikingelectronics.shared.pagination.PaginationHolder
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow



interface ActivityRepository {
    fun getAllEntries(): Flow<ActivityEntry>
    suspend fun fetchEntries(lastEntry: DocumentSnapshot? = null): PaginationHolder<ActivityEntry>
}

//@UserScope
class ActivityRepositoryImpl constructor(
    val db: FirebaseFirestore,
//    override val storage: FirebaseStorage,
    val user: User,
    val sipAccount: SipAccount
): FirebaseRepository(), ActivityRepository {

    override suspend fun fetchEntries(lastEntry: DocumentSnapshot?): PaginationHolder<ActivityEntry> {
        var query = db.collection("activity")
            .where("sourceDevice", inArray = sipAccount.devices)
            .orderBy("timestamp", Direction.DESCENDING)
            .limit(25)

        lastEntry?.let {
            query = query.startAfter(it)
        }

        val docs = query.get().documents
        val entries = docs.map { it.data(ActivityEntry.serializer()) }

        return PaginationHolder(entries, docs.last())
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
                "timestamp" to FieldValue.serverTimestamp
            )

            activityCollectionRef.add(entry).addOnSuccessListener {
                Timber.d("Entry added: ${it.id}")
            }
        }
    }
}