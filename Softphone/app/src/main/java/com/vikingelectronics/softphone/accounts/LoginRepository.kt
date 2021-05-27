package com.vikingelectronics.softphone.accounts

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.vikingelectronics.softphone.dagger.UserComponent
import com.vikingelectronics.softphone.extensions.timber
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Provider

class LoginRepository @Inject constructor(
    db: FirebaseFirestore
) {
    private val userCollectionRef = db.collection("users")
    private val sipCollectionRef = db.collection("sipAccounts")


    suspend fun fetchOrCreateUserAccount(username: String, pushToken: String = ""): DocumentReference {

    }


    suspend fun fetchOrCreateSipAccount(userRef: DocumentReference, base: String): SipAccount? {
        val doc = sipCollectionRef.document(base)
        val docExists = doc.get().await().exists()

        if (!docExists) doc.apply {
            val sipData = mapOf(
                "users" to listOf(userRef),
            )
            set(sipData).await()
        }

        return getAwaitObject(doc)
    }

//     suspend fun attemptSipFetch(base: String): DocumentReference? {
//         return try {
//             val doc = sipCollectionRef.document(base)
//             val exists = doc.get().await().exists()
//             if (exists) doc else null
//         } catch (e: Exception) {
//             null
//         }
//    }
//
//     suspend fun createSipAccount(userRef: DocumentReference, base: String): DocumentReference {
//        val sipData = mapOf(
//            "users" to listOf(userRef),
//        )
//
//         return sipCollectionRef.document(base).apply {
//             set(sipData).await()
//         }
//    }

     suspend fun attemptUserFetch(username: String): DocumentReference? {
        return try {
            userCollectionRef.whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()
                .documents[0]
                .reference
        } catch (e: Exception) {
            null
        }
    }

     suspend fun createUserAccount(username: String, pushToken: String = ""): DocumentReference {
        val userData = mapOf(
            "username" to username,
            "pushToken" to pushToken
        )

        return userCollectionRef.add(userData).await()
    }

     fun associateSipAccount(
         user: User,
         userReference: DocumentReference,
         sipReference: DocumentReference
     ) {
         user.sipAccount = sipReference

         userReference.set(user)
     }

    suspend inline fun <reified T> getAwaitObject(reference: DocumentReference): T? {
      return reference.get().await().toObject(T::class.java)
    }
}