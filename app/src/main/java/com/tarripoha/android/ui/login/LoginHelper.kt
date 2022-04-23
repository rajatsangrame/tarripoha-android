package com.tarripoha.android.ui.login

import android.app.Activity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit

object LoginHelper {

    private const val TAG = "FirebaseAuth"
    private const val PHONE_AUTH_TIMEOUT = 30L
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    fun processOtpLogin(
        phone: String,
        activity: Activity,
        callbacks: OnVerificationStateChangedCallbacks
    ) {
        auth.setLanguageCode("en")
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(PHONE_AUTH_TIMEOUT, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(
        verificationId: String,
        otp: String,
        activity: Activity,
        callback: OnCompleteListener<AuthResult>
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity, callback)
    }

    fun logoutUser() {
        auth.signOut()
    }

}
