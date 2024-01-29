package com.tarripoha.android.util.ktx

import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object FirebaseUtil {

    suspend fun Query.fireStoreFind(): QuerySnapshot {
        return suspendCoroutine { continuation ->
            this.get().addOnSuccessListener { documents ->
                continuation.resume(documents)
            }.addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
        }
    }


}