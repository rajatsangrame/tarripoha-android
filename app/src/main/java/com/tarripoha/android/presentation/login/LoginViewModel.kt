package com.tarripoha.android.presentation.login

import android.app.Activity
import android.app.Application
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.DataSnapshot
import com.tarripoha.android.R
import com.tarripoha.android.data.datasource.home.HomeUseCase
import com.tarripoha.android.presentation.base.BaseViewModel
import com.tarripoha.android.util.TPUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    resources: Resources
) : BaseViewModel(resources) {

    private var phoneNumber: String? = null
    private var storedVerificationId: String? = null
    private var resendToken: ForceResendingToken? = null
    private val isCodeSent = MutableLiveData<Boolean>()
    private val isNewUserCreated = MutableLiveData<Boolean>()
    private val isDirtyAccount = MutableLiveData<Boolean>()

    fun getIsCodeSent(): LiveData<Boolean> = isCodeSent

    fun getIsNewUserCreated(): LiveData<Boolean> = isNewUserCreated

    fun getIsDirtyAccount(): LiveData<Boolean> = isDirtyAccount

    fun processLogin(
        phone: String,
        activity: Activity,
    ) {
        Timber.tag(TAG).i("processLogin: $phone")
        this.showProgress.value = true
        LoginHelper.processOtpLogin(
            phone = phone,
            activity = activity,
            callbacks = object : OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    Timber.tag(TAG).i("onVerificationCompleted: $phone $p0")
                    this@LoginViewModel.showProgress.value = false
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Timber.tag(TAG).e("onVerificationFailed: $phone ${e.message}")
                    this@LoginViewModel.showProgress.value = false
                    setUserMessage(getString(R.string.error_otp_verification_failed))
                    if (e is FirebaseAuthInvalidCredentialsException) {
                        Timber.tag(TAG).e("onVerificationFailed: $e")
                    } else if (e is FirebaseTooManyRequestsException) {
                        Timber.tag(TAG).e("onVerificationFailed: $e")
                    }
                }

                override fun onCodeSent(
                    id: String,
                    token: ForceResendingToken
                ) {
                    Timber.tag(TAG).i("onCodeSent")
                    this@LoginViewModel.showProgress.value = false
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
        this.showProgress.value = true
        LoginHelper.verifyOtp(storedVerificationId!!, otp, activity) { task ->
            if (task.isSuccessful) {
                val user = task.result?.user
                Timber.tag(TAG).d("verifyOtp: success ${user.toString()}")
                fetchUserInfo()
            } else {
                this.showProgress.value = false
                Timber.tag(TAG).e("verifyOtp: failure ${task.exception}")
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    setUserMessage(getString(R.string.error_incorrect_otp))
                }
            }
        }
    }

    private fun fetchUserInfo() {
        Timber.tag(TAG).i("fetching user info: ")
        if (phoneNumber == null) {
            setUserMessage(getString(R.string.error_unknown))
            this.showProgress.value = false
            return
        }
        repository.fetchUserInfo(
            phone = phoneNumber!!, success = {
                fetchUserInfoResponse(it)
            }, failure = {
                this.showProgress.value = false
                setUserMessage(getString(R.string.error_unable_to_fetch))
            }, connectionStatus = {
                if (!it) this.showProgress.value = false
            })
    }

    private fun fetchUserInfoResponse(
        snapshot: DataSnapshot,
    ) {
        Timber.tag(TAG).i("fetchUserInfoResponse: ")
        try {
            if (snapshot.childrenCount > 0) {
                if (snapshot.getValue(User::class.java) != null) {
                    this.showProgress.value = false
                    val user: User = snapshot.getValue(User::class.java)!!
                    val isDirty = user.dirty
                    if (isDirty != null && isDirty) {
                        setUserMessage(getString(R.string.msg_user_blocked, user.name))
                        isDirtyAccount.value = true
                        return
                    }
                    Timber.tag(TAG).i("fetchUserInfoResponse: user found ${user.phone}")
                    this.setUser(user)
                }
            } else {
                this.showProgress.value = false
                isNewUserCreated.value = true
            }
        } catch (e: Exception) {
            this.showProgress.value = false
            Timber.tag(TAG).e("fetchAllResponse: $e")
        }
    }

    fun createUser(
        name: String,
        email: String
    ) {
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
        this.showProgress.value = null
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}
