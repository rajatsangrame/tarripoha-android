package com.tarripoha.android.presentation.base

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarripoha.android.R
import com.tarripoha.android.util.TPUtils

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

abstract class BaseViewModel(private val resources: Resources) : ViewModel() {

    private val userMessage: MutableLiveData<String> = MutableLiveData()
    val isRefreshing: MutableLiveData<Boolean> = MutableLiveData()
    val showProgress = MutableLiveData<Boolean>()
    //private val user = MutableLiveData<User>()

    fun setUserMessage(msg: String) {
        userMessage.value = msg
        Log.i(TAG, "setUserMessage: $msg")
    }

    fun getUserMessage() = userMessage

    fun getShowProgress(): LiveData<Boolean> = showProgress

    fun isRefreshing(): LiveData<Boolean> = isRefreshing

    fun getString(resId: Int) = resources.getString(resId)

    fun getString(
        resId: Int,
        value: String
    ) = resources.getString(resId, value)


    /*fun setUser(user: User?) {
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
    }*/

    companion object {
        private const val TAG = "BaseViewModel"
    }
}
