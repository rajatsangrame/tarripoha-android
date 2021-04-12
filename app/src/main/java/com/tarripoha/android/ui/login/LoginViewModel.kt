package com.tarripoha.android.ui.login

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.tarripoha.android.R
import com.tarripoha.android.data.Repository
import com.tarripoha.android.ui.BaseViewModel
import javax.inject.Inject

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

class LoginViewModel @Inject constructor(
  var repository: Repository,
  app: Application
) : BaseViewModel(app) {

  private val isCodeSent = MutableLiveData<Boolean>()
  private var storedVerificationId: String? = null
  private var resendToken: ForceResendingToken? = null

  fun getIsCodeSent() = isCodeSent

  fun processLogin(
    phone: String,
    activity: Activity,
  ) {
    if (!isInternetConnected()) {
      return
    }
    Log.i(TAG, "processLogin: $phone")

    LoginHelper.processOtpLogin(
        phone = phone,
        activity = activity,
        callbacks = object : OnVerificationStateChangedCallbacks() {
          override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            Log.i(TAG, "onVerificationCompleted: $phone")
          }

          override fun onVerificationFailed(e: FirebaseException) {
            Log.e(TAG, "onVerificationFailed: $phone ${e.message}")
            if (e is FirebaseAuthInvalidCredentialsException) {
              // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
              // The SMS quota for the project has been exceeded
            }
          }

          override fun onCodeSent(
            id: String,
            token: ForceResendingToken
          ) {
            super.onCodeSent(id, token)
            storedVerificationId = id
            resendToken = token
            isCodeSent.value = true
            Log.i(TAG, "onCodeSent")
          }
        }
    )
  }

  fun verifyOtp(
    otp: String,
    activity: Activity
  ) {
    if (storedVerificationId == null) {
      setUserMessage(getString(R.string.error_unknown))
      return
    }
    LoginHelper.verifyOtp(storedVerificationId!!, otp, activity) { task ->
      if (task.isSuccessful) {
        // Sign in success, update UI with the signed-in user's information
        val user = task.result?.user
        Log.d(TAG, "verifyOtp: success ${user.toString()}")

      } else {
        // Sign in failed, display a message and update the UI
        Log.w(TAG, "verifyOtp: failure", task.exception)
        if (task.exception is FirebaseAuthInvalidCredentialsException) {
          // The verification code entered was invalid
        }
        // Update UI
      }
    }
  }

  companion object {
    private const val TAG = "LoginViewModel"
  }
}
