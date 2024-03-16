package com.tarripoha.android.presentation.base

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarripoha.android.domain.entity.User
import com.tarripoha.android.util.errorhandler.AppError
import com.tarripoha.android.util.helper.UserHelper
import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

abstract class BaseViewModel(private val resources: Resources) : ViewModel() {

    private val userMessage: MutableLiveData<String> = MutableLiveData()
    private val errorMessage: MutableLiveData<String> = MutableLiveData()

    private val isRefreshing: MutableLiveData<Boolean> = MutableLiveData()
    val showProgress = MutableLiveData<Boolean>()
    private val user = MutableLiveData<User>()

    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        val error = AppError.parse(throwable)
        errorMessage.postValue(getString(error.message))
    }

    fun setUserMessage(msg: String) {
        userMessage.value = msg
        Timber.tag(TAG).i("setUserMessage: $msg")
    }

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun getUserMessage(): LiveData<String> = userMessage

    fun isRefreshing(): LiveData<Boolean> = isRefreshing

    fun getString(resId: Int) = resources.getString(resId)

    fun getString(
        resId: Int,
        value: String
    ) = resources.getString(resId, value)


    fun setUser(user: User?) {
        this.user.value = user
    }

    fun getUser() = user

    fun getPrefUser() = UserHelper.getUser()

    fun isUserAdmin(): Boolean {
        UserHelper.getUser()
            ?.let {
                return@isUserAdmin it.admin
            }
        return false
    }

    fun isUserLogin(): Boolean {
        return UserHelper.isLoggedIn()
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}
