package com.tarripoha.android.presentation.login

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.tarripoha.android.R
import com.tarripoha.android.data.datasource.user.UserUseCases
import com.tarripoha.android.domain.entity.User
import com.tarripoha.android.presentation.base.BaseViewModel
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.helper.UserHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val savedStateHandle: SavedStateHandle,
    resources: Resources
) : BaseViewModel(resources) {

    var phoneNumber: String? = null
    var storedVerificationId: String? = null
    var resendToken: ForceResendingToken? = null
    private val navigateToNewUserScreen = MutableLiveData<Boolean>()
    private val isDirtyAccount = MutableLiveData<Boolean>()
    private val isUserCreated = MutableLiveData<Boolean>()

    fun getNavigateToNewUserScreen(): LiveData<Boolean> = navigateToNewUserScreen

    fun getIsDirtyAccount(): LiveData<Boolean> = isDirtyAccount

    fun getIsUserCreated(): LiveData<Boolean> = isUserCreated

    fun checkIfUserInfoExist() {
        Timber.tag(TAG).i("fetching user info: ")
        if (phoneNumber == null) {
            setUserMessage(getString(R.string.error_unknown))
            showProgress.value = false
            return
        }
        viewModelScope.launch(exceptionHandler) {
            async(Dispatchers.IO) {
                val user = userUseCases.getUser(phoneNumber!!)
                if (user == null) {
                    navigateToNewUserScreen.value = true
                } else if (user.dirty == true) {
                    setUserMessage(getString(R.string.msg_user_blocked))
                    isDirtyAccount.value= true
                } else UserHelper.setUser(user)
            }.await()
            showProgress.value = false
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
        viewModelScope.launch(exceptionHandler) {
            async(Dispatchers.IO) {
                userUseCases.createUser(user)
                UserHelper.setUser(user)
            }.await()
            isUserCreated.value = true
        }
    }

    fun resetLoginParams() {
        phoneNumber = null
        storedVerificationId = null
        resendToken = null
        navigateToNewUserScreen.value = false
        isDirtyAccount.value = false
        isUserCreated.value = false
        this.showProgress.value = null
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}
