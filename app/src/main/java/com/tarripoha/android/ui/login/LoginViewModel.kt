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
import com.tarripoha.android.util.TPUtils
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

class LoginViewModel @Inject constructor(
    private var repository: Repository,
    app: Application
) : BaseViewModel(app) {

    private var phoneNumber: String? = null
    private var storedVerificationId: String? = null
    private var resendToken: ForceResendingToken? = null
    private val isCodeSent = MutableLiveData<Boolean>()
    private val isNewUserCreated = MutableLiveData<Boolean>()
    private val isDirtyAccount = MutableLiveData<Boolean>()

    fun getIsCodeSent() = isCodeSent

    fun getIsNewUserCreated() = isNewUserCreated

    fun getIsDirtyAccount() = isDirtyAccount

    fun processLogin(
        phone: String,
        activity: Activity,
    ) {
        if (!checkNetworkAndShowError()) {
            return
        }
        Log.i(TAG, "processLogin: $phone")

        setShowProgress(true)
        LoginHelper.processOtpLogin(
            phone = phone,
            activity = activity,
            callbacks = object : OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    Log.i(TAG, "onVerificationCompleted: $phone $p0")
                    setShowProgress(false)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "onVerificationFailed: $phone ${e.message}")
                    setShowProgress(false)
                    setUserMessage(getString(R.string.error_otp_verification_failed))
                    if (e is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        Log.e(TAG, "onVerificationFailed: FirebaseAuthInvalidCredentialsException")
                    } else if (e is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        Log.e(TAG, "onVerificationFailed: FirebaseTooManyRequestsException")
                    }
                }

                override fun onCodeSent(
                    id: String,
                    token: ForceResendingToken
                ) {
                    Log.i(TAG, "onCodeSent")
                    setShowProgress(false)
                    phoneNumber = phone
                    storedVerificationId = id
                    resendToken = token
                    isCodeSent.value = true
                    super.onCodeSent(id, token)
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
        setShowProgress(true)
        LoginHelper.verifyOtp(storedVerificationId!!, otp, activity) { task ->
            if (task.isSuccessful) {
                val user = task.result?.user
                Log.d(TAG, "verifyOtp: success ${user.toString()}")
                fetchUserInfo()
            } else {
                setShowProgress(false)
                Log.e(TAG, "verifyOtp: failure", task.exception)
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    setUserMessage(getString(R.string.error_incorrect_otp))
                }
            }
        }
    }

    private fun fetchUserInfo() {
        if (!checkNetworkAndShowError()) {
            setShowProgress(false)
            return
        }
        Log.i(TAG, "fetchUserInfo: ")
        if (phoneNumber == null) {
            setUserMessage(getString(R.string.error_unknown))
            setShowProgress(false)
            return
        }
        repository.fetchUserInfo(
            phone = phoneNumber!!, success = {
                fetchUserInfoResponse(it)
            }, failure = {
                setShowProgress(false)
                setUserMessage(getString(R.string.error_unable_to_fetch))
            }, connectionStatus = {
                if (!it) setShowProgress(false)
            })
    }

    private fun fetchUserInfoResponse(
        snapshot: DataSnapshot,
    ) {
        Log.i(TAG, "fetchUserInfoResponse: ")
        try {
            if (snapshot.childrenCount > 0) {
                if (snapshot.getValue(User::class.java) != null) {
                    setShowProgress(false)
                    val user: User = snapshot.getValue(User::class.java)!!
                    val isDirty = user.dirty
                    if (isDirty != null && isDirty) {
                        setUserMessage(getString(R.string.msg_user_blocked, user.name))
                        isDirtyAccount.value = true
                        return
                    }
                    Log.i(TAG, "fetchUserInfoResponse: user found ${user.phone}")
                    this.setUser(user)
                }
            } else {
                setShowProgress(false)
                isNewUserCreated.value = true
            }
        } catch (e: Exception) {
            setShowProgress(false)
            Log.e(TAG, "fetchAllResponse: ${e.localizedMessage}")
        }
    }

    fun createUser(
        name: String,
        email: String
    ) {
        if (!checkNetworkAndShowError()) {
            return
        }
        if (phoneNumber.isNullOrEmpty() || name.isEmpty() || email.isEmpty()) {
            setUserMessage(getString(R.string.error_unknown))
            return
        }
        val user = User(
            id = TPUtils.getRandomUuid(),
            name = name,
            phone = phoneNumber!!,
            email = email,
            timestamp = System.currentTimeMillis()
        )
        repository.createUser(
            phone = phoneNumber!!,
            user = user,
            success = {
                setUserMessage(getString(R.string.msg_user_created, user.name))
                this.setUser(user)
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
        isNewUserCreated.value = false
        isDirtyAccount.value = false
        setShowProgress(null)
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}
