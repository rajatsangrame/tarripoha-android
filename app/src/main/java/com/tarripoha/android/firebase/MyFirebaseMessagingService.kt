package com.tarripoha.android.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tarripoha.android.util.helper.UserHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {


    /**
     * Called when a message is received.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }


    /**
     * Called when a new token for the default Firebase project is generated.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val user = UserHelper.getUser()
        FcmUtil.uploadFCMToken(user = user,
            success = {
                Log.i(TAG, "onNewToken: uploaded")
            }, failure = {
                Log.e(TAG, "onNewToken: failed")
            })
    }


    companion object {
        private const val TAG = "MyFirebaseMessagingServ"
    }
}