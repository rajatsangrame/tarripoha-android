package com.tarripoha.android.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarripoha.android.R
import com.tarripoha.android.data.model.User
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.helper.UserHelper

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

abstract class BaseViewModel(val app: Application) : ViewModel() {

    private val userMessage: MutableLiveData<String> = MutableLiveData()
    private val showProgress = MutableLiveData<Boolean>()
    private val user = MutableLiveData<User>()

    fun setUserMessage(msg: String) {
        userMessage.value = msg
        Log.i(TAG, "setUserMessage: $msg")
    }

    fun getUserMessage() = userMessage

    fun setShowProgress(showProgress: Boolean?) {
        this.showProgress.value = showProgress
    }

    fun getShowProgress() = showProgress

    fun getString(resId: Int) = app.applicationContext.getString(resId)

    fun getString(
        resId: Int,
        value: String
    ) = app.applicationContext.getString(resId, value)

    fun getContext(): Context = app.applicationContext

    fun setUser(user: User?) {
        this.user.value = user
    }

    fun getUser() = user

    fun getUserName(): String? = UserHelper.getUser()?.name

    fun isUserAdmin(): Boolean {
        UserHelper.getUser()
            ?.let {
                return@isUserAdmin it.admin
            }
        return false
    }

    fun checkNetworkAndShowError(): Boolean {
        if (!TPUtils.isNetworkAvailable(getContext())) {
            setUserMessage(getString(R.string.error_no_internet))
            return false
        }
        return true
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}
