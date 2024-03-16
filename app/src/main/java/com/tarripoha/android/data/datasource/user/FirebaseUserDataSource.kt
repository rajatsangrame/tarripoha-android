package com.tarripoha.android.data.datasource.user

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.tarripoha.android.R
import com.tarripoha.android.domain.entity.User
import com.tarripoha.android.domain.repository.user.UserDataSource
import com.tarripoha.android.util.helper.LoginHelper
import com.tarripoha.android.presentation.login.LoginViewModel
import timber.log.Timber

class FirebaseUserDataSource(
    private val reference: DatabaseReference,
    private val auth: FirebaseAuth
) : UserDataSource {

    override suspend fun createUser(user: User) {
        TODO("Not yet implemented")
    }

    override suspend fun getUser(): User {
        TODO("Not yet implemented")
    }

    override suspend fun loginUser() {
        LoginHelper.processOtpLogin(
            phone = phone,
            activity = activity,
            callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    Timber.tag(LoginViewModel.TAG).i("onVerificationCompleted: $phone $p0")
                    this@LoginViewModel.showProgress.value = false
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Timber.tag(LoginViewModel.TAG).e("onVerificationFailed: $phone ${e.message}")
                    this@LoginViewModel.showProgress.value = false
                    setUserMessage(getString(R.string.error_otp_verification_failed))
                    if (e is FirebaseAuthInvalidCredentialsException) {
                        Timber.tag(LoginViewModel.TAG).e("onVerificationFailed: $e")
                    } else if (e is FirebaseTooManyRequestsException) {
                        Timber.tag(LoginViewModel.TAG).e("onVerificationFailed: $e")
                    }
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Timber.tag(LoginViewModel.TAG).i("onCodeSent")
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

    override suspend fun verifyOtp() {
        TODO("Not yet implemented")
    }
}