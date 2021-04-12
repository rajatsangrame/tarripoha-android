package com.tarripoha.android.ui.login

import android.app.Activity
import android.media.MediaPlayer.OnCompletionListener
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
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
    auth.languageCode = "en"
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

}
