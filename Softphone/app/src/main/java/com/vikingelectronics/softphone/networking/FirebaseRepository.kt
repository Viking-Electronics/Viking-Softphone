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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

abstract class FirebaseRepository {

    data class PaginationHolder<T> (val entries: List<T>, val index: DocumentSnapshot?)
    sealed class ListState<out T> {
        class Success<T>(val list: List<T>): ListState<T>()
        object Loading: ListState<Nothing>()
        class Failure(val e: Exception): ListState<Nothing>()
    }

    internal val FAVORITE_KEY = "isFavorite"

    internal abstract val db: FirebaseFirestore
    internal abstract val storage: FirebaseStorage
    internal abstract val user: User
    internal abstract val sipAccount: SipAccount

    internal val devicesCollectionRef by lazy { db.collection("devices") }
    internal val activityCollectionRef by lazy { db.collection("activity") }

    val storageRef: StorageReference by lazy { storage.reference.child(sipAccount.id) }


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

    inline fun <reified T> List<DocumentSnapshot>.iterateToObjectList(): List<T> {
        map {  }
        val innerList = mutableListOf<T>()
        this.iterateToObject<T> {
            innerList.add(it)
        }
        return innerList
    }

    inline fun <reified T> List<DocumentSnapshot>.iterateActorToObjectList(actor: (List<T>) -> Unit) {
        actor(iterateToObjectList())
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