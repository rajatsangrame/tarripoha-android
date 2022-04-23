package com.tarripoha.android.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.tarripoha.android.data.model.User

object FcmUtil {

    private const val TAG = "FcmUtil"

    fun uploadFCMToken(user: User?, success: () -> Unit, failure: () -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
            if (!task.isSuccessful) {
                Log.e(TAG, "processFcmTokenUpload: Fetching FCM registration token failed")
                return@addOnCompleteListener
            }
            val fcmToken = task.result
            val userRef: DatabaseReference = Firebase.database.getReference("user")
            user?.phone?.let {
                userRef.child(it).child("fcmToken").setValue(fcmToken).addOnSuccessListener {
                    success()
                }.addOnCanceledListener {
                    failure()
                }.addOnFailureListener {
                    failure()
                }
            }
        }
    }
}