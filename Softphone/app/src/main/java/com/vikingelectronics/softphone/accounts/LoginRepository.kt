package com.vikingelectronics.softphone.accounts

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.vikingelectronics.shared.accounts.SipAccount
import com.vikingelectronics.shared.accounts.User
import com.vikingelectronics.softphone.data.FirestoreObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginRepository @Inject constructor(
    db: FirebaseFirestore
) {
    private val userCollectionRef = db.collection("users")
    private val sipCollectionRef = db.collection("sipAccounts")


    suspend fun fetchOrCreateUserAccount(username: String, pushToken: String = ""): FirestoreObject<User> {
        val query = userCollectionRef.whereEqualTo("username", username).limit(1)
        val querySnapshot = query.get().await()
        val userExists = !querySnapshot.isEmpty && !querySnapshot.documents[0].exists()

        val resultDoc = if (userExists) querySnapshot.documents[0].reference else {
            val userData = mapOf(
                "username" to username,
                "pushToken" to pushToken
            )

            userCollectionRef.add(userData).await()
        }

        return FirestoreObject(resultDoc, User::class.java).apply {
            getObj()?.let {
                if (it.pushToken != pushToken) updateUserPushToken(it, pushToken)
            }
        }
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

//         user.sipAccount = sipRepresentation.reference

         userRepresentation.reference.set(user).await()
     }

    suspend fun updateUserPushToken(user: User, pushToken: String) {
        val updatedUser = user.copy(pushToken = pushToken)
        userCollectionRef.document(user.id).set(updatedUser).await()
    }
}