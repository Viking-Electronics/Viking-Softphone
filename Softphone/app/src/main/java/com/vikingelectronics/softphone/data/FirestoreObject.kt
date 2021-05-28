package com.vikingelectronics.softphone.data

import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.tasks.await

data class FirestoreObject<T>(val reference: DocumentReference, val clazz: Class<T>) {
    suspend fun getObj(): T? = reference.get().await().toObject(clazz)
}