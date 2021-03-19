package com.vikingelectronics.softphone.networking

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.vikingelectronics.softphone.accounts.SipAccount
import com.vikingelectronics.softphone.accounts.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

abstract class FirebaseRepository {

    internal val SECOND_MILLIS = 1000
    internal val MINUTE_MILLIS = 60 * SECOND_MILLIS
    internal val HOUR_MILLIS = 60 * MINUTE_MILLIS
    internal val DAY_MILLIS = 24 * HOUR_MILLIS
    internal val FAVORITE_KEY = "isFavorite"

    internal abstract val db: FirebaseFirestore
    internal abstract val storage: FirebaseStorage

    internal val userCollectionRef by lazy { db.collection("users") }
    internal val sipCollectionRef by lazy { db.collection("sipAccounts") }
    internal val devicesCollectionRef by lazy { db.collection("devices") }
    internal val activityCollectionRef by lazy { db.collection("activity") }

    internal var storageRef: StorageReference? = null
        private set


    internal suspend fun initStorageRecord() {
        if (storageRef != null) return

        val user = getUser("5514255221u1") ?: return
        val sipAccount = getSipAccount(user) ?: return

        storageRef = storage.reference.child(sipAccount.id)
    }

    internal suspend fun getUser(username: String): User? {
        return try {
            userCollectionRef.whereEqualTo("username", username)
                .limit(1)
                .getAwait()
                .toObjects<User>()[0]
        } catch (e: Exception) {
            null
        }
    }

    internal suspend fun getSipAccount(user: User): SipAccount? {
        return user.sipAccount.get().await().toObject<SipAccount>()
    }

    suspend fun Query.getAwait(): QuerySnapshot = this.get().await()
    suspend fun DocumentReference.getAwait(): DocumentSnapshot = this.get().await()

    suspend fun List<DocumentReference>.iterate(actor: (DocumentSnapshot) -> Unit) = this.forEach {
        actor.invoke(it.getAwait())
    }
    suspend inline fun <reified T> List<DocumentReference>.iterateToObject(actor: (T) -> Unit) = this.forEach {
        it.getAwait().toObject<T>()?.let(actor)
    }

    inline fun <reified T> List<DocumentSnapshot>.iterateToObject(actor: (T) -> Unit) = this.forEach {
        it.toObject<T>()?.let(actor)
    }

    fun Task<StorageMetadata>.emitResult() = callbackFlow<Result<Boolean>> {
        addOnSuccessListener {
            offer(Result.success(true))
        }
        addOnFailureListener {
            offer(Result.failure(it.fillInStackTrace()))
        }
        addOnCanceledListener {
            offer(Result.success(false))
        }
        awaitClose()
    }
}