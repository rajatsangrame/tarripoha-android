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
import com.google.firebase.database.DataSnapshot
import com.tarripoha.android.R
import com.tarripoha.android.data.Repository
import com.tarripoha.android.data.model.User
import com.tarripoha.android.ui.BaseViewModel
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

class LoginViewModel @Inject constructor(
  var repository: Repository,
  app: Application
) : BaseViewModel(app) {

  private var phoneNumber: String? = null
  private var storedVerificationId: String? = null
  private var resendToken: ForceResendingToken? = null
  private val isCodeSent = MutableLiveData<Boolean>()
  private val createNewUser = MutableLiveData<Boolean>()

  fun getIsCodeSent() = isCodeSent

  fun getCreateNewUser() = createNewUser

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
            Log.i(TAG, "onVerificationCompleted: $phone $p0")
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
            phoneNumber = phone
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
        val user = task.result?.user
        Log.d(TAG, "verifyOtp: success ${user.toString()}")
        fetchUserInfo()
      } else {
        Log.w(TAG, "verifyOtp: failure", task.exception)
        if (task.exception is FirebaseAuthInvalidCredentialsException) {
        }
      }
    }
  }

  private fun fetchUserInfo() {
    if (!isInternetConnected()) {
      return
    }
    Log.i(TAG, "fetchUserInfo: ")
    if (phoneNumber == null) {
      setUserMessage(getString(R.string.error_unknown))
      return
    }
    repository.fetchUserInfo(
        phone = phoneNumber!!, success = {
      fetchUserInfoResponse(it)
    }, failure = {
      setUserMessage(getString(R.string.error_unable_to_fetch))
    }, connectionStatus = {

    })
  }

  private fun fetchUserInfoResponse(
    snapshot: DataSnapshot,
  ) {
    Log.i(TAG, "fetchUserInfoResponse: ")
    try {
      if (snapshot.childrenCount > 0) {
        if (snapshot.getValue(User::class.java) != null
        ) {
          val user: User = snapshot.getValue(User::class.java)!!
          val isDirty = user.dirty
          if (isDirty == null || !isDirty) {
            setUserMessage(getString(R.string.msg_user_blocked, user.name))
            return
          }
          Log.i(TAG, "fetchUserInfoResponse: user found ${user.phone}")
          this.setUser(user)
        }
      } else {
        createNewUser.value = true
      }
    } catch (e: Exception) {
      Log.e(TAG, "fetchAllResponse: ${e.localizedMessage}")
    }
  }

  private fun createUser(
    phone: String,
    user: User
  ) {
    if (!isInternetConnected()) {
      return
    }
    repository.createUser(
        phone = phone,
        user = user,
        success = {
          setUserMessage(getString(R.string.msg_user_created, user.name))
        }, failure = {
      setUserMessage(getString(R.string.error_unable_to_fetch))
    }, connectionStatus = {

    })
  }

  fun resetLoginParams() {
    phoneNumber = null
    storedVerificationId = null
    resendToken = null
    isCodeSent.value = false
    createNewUser.value = false
  }

  companion object {
    private const val TAG = "LoginViewModel"
  }
}
