package com.vikingelectronics.softphone.accounts

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.vikingelectronics.softphone.dagger.UserComponent
import com.vikingelectronics.softphone.extensions.timber
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Provider

class LoginRepository @Inject constructor(
    private val userComponentProvider: Provider<UserComponent.Builder>,
    db: FirebaseFirestore
) {
    private val userCollectionRef = db.collection("users")
    private val sipCollectionRef = db.collection("sipAccounts")


    fun buildUserComponent(sipAccount: SipAccount, user: User): UserComponent {
        return userComponentProvider.get().setUser(user).setSip(sipAccount).build()
    }

     suspend fun attemptSipFetch(base: String): DocumentReference? {
         return try {
             val doc = sipCollectionRef.document(base)
             val refData = doc.get().await().data
             if (refData != null) doc else null
         } catch (e: Exception) {
             null
         }
    }

     suspend fun createSipAccount(userRef: DocumentReference, base: String): DocumentReference {
        val sipData = mapOf(
            "users" to listOf(userRef),
        )

         return sipCollectionRef.document(base).apply {
             set(sipData).await()
         }
    }

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

     suspend fun createUserAccount(username: String): DocumentReference {
        val userData = mapOf(
            "username" to username,
            "pushToken" to ""
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