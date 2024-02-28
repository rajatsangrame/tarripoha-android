package com.tarripoha.android.util.helper

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.tarripoha.android.util.ktx.parseObject
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object FirebaseHelper {

    suspend fun Query.cloudStorageFind(): QuerySnapshot {
        return suspendCoroutine { continuation ->
            this.get().addOnSuccessListener { documents ->
                continuation.resume(documents)
            }.addOnFailureListener { e ->
                Timber.e(e)
                continuation.resumeWithException(e)
            }
        }
    }

    suspend inline fun <reified T> com.google.firebase.database.Query.databaseFind(): T {
        return suspendCoroutine { continuation ->
            this.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.value.toString().parseObject<T>()
                    continuation.resume(data)
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException())
                    continuation.resumeWithException(error.toException())
                }
            })
        }
    }

}