package com.vikingelectronics.softphone.networking

import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.vikingelectronics.softphone.accounts.SipAccount
import com.vikingelectronics.softphone.accounts.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

abstract class FirebaseRepository {

    internal abstract val db: FirebaseFirestore

    internal val userCollectionRef by lazy { db.collection("users") }
    internal val sipCollectionRef by lazy { db.collection("sipAccounts") }
    internal val devicesCollectionRef by lazy { db.collection("devices") }
    internal val activityCollection by lazy { db.collection("activity") }

    internal suspend fun getUser(username: String): User {
        val userDoc = userCollectionRef.whereEqualTo("username", username)
            .limit(1)
            .get()
            .await()

        return userDoc.toObjects<User>()[0]
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
}