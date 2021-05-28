package com.vikingelectronics.softphone.accounts

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.vikingelectronics.softphone.dagger.UserComponent
import com.vikingelectronics.softphone.data.FirestoreObject
import com.vikingelectronics.softphone.extensions.timber
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Provider

class LoginRepository @Inject constructor(
    db: FirebaseFirestore
) {
    private val userCollectionRef = db.collection("users")
    private val sipCollectionRef = db.collection("sipAccounts")


    suspend fun fetchOrCreateUserAccount(username: String, pushToken: String = ""): FirestoreObject<User> {
        val query = userCollectionRef.whereEqualTo("username", username).limit(1)
        val resultSnapshot = query.get().await().documents[0]
        val userExists = resultSnapshot.exists()

        val resultDoc = if (userExists) resultSnapshot.reference else {
            val userData = mapOf(
                "username" to username,
                "pushToken" to pushToken
            )

            userCollectionRef.add(userData).await()
        }

        return FirestoreObject(resultDoc, User::class.java)
    }


    suspend fun fetchOrCreateSipAccount(userRef: DocumentReference, base: String): FirestoreObject<SipAccount> {
        val doc = sipCollectionRef.document(base)
        val docExists = doc.get().await().exists()

        if (!docExists) doc.apply {
            val sipData = mapOf(
                "users" to listOf(userRef),
            )
            set(sipData).await()
        }

        return FirestoreObject(doc, SipAccount::class.java)
    }


     suspend fun associateSipAccountIfNecessary(
         user: User,
         userRepresentation: FirestoreObject<User>,
         sipRepresentation: FirestoreObject<SipAccount>
     ) {
         if (user.sipAccountExists()) return

         user.sipAccount = sipRepresentation.reference

         userRepresentation.reference.set(user).await()
     }
}