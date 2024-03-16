package com.tarripoha.android.util.helper

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.tarripoha.android.util.ktx.parseObject
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object FirebaseHelper {

    object CloudStore {
        fun Query.buildWhereEqualTo(map: Map<String, Any>?): Query {
            if (map.isNullOrEmpty()) return this
            var query = this
            map.entries.forEach {
                query = query.whereEqualTo(it.key, it.value)
            }
            return query
        }

        suspend inline fun <reified T> Query.findMany(): List<T> {
            return suspendCoroutine { continuation ->
                this.get().addOnSuccessListener { documents ->
                    val documentList: List<T> = documents.map { document ->
                        document.toObject(T::class.java)
                    }
                    continuation.resume(documentList)
                }.addOnFailureListener { e ->
                    Timber.e(e)
                    continuation.resumeWithException(e)
                }
            }
        }

        suspend inline fun Task<Void>.execute() {
            return suspendCoroutine { continuation ->
                this.addOnSuccessListener {
                    continuation.resume(Unit)
                }.addOnFailureListener { e ->
                    Timber.e(e)
                    continuation.resumeWithException(e)
                }
            }
        }

        suspend inline fun <reified T> Task<DocumentSnapshot>.findOne(): T {
            return suspendCoroutine { continuation ->
                this.addOnSuccessListener {
                    val snapshot = it.toString().parseObject<T>()
                    continuation.resume(snapshot)
                }.addOnFailureListener { e ->
                    Timber.e(e)
                    continuation.resumeWithException(e)
                }
            }
        }
    }


    object RealtimeDatabase {
        suspend inline fun <reified T> com.google.firebase.database.Query.findItems(): T {
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


}