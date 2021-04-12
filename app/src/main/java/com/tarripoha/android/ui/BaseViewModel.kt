package com.tarripoha.android.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarripoha.android.R
import com.tarripoha.android.data.model.User
import com.tarripoha.android.util.TPUtils

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

abstract class BaseViewModel(val app: Application) : ViewModel() {

  private val userMessage: MutableLiveData<String> = MutableLiveData()

  private val user = MutableLiveData<User>()

  fun setUserMessage(msg: String) {
    userMessage.value = msg
  }

  fun getUserMessage() = userMessage

  fun getString(resId: Int) = app.applicationContext.getString(resId)

  fun getString(
    resId: Int,
    value: String
  ) = app.applicationContext.getString(resId, value)

  fun getContext(): Context = app.applicationContext

  fun setUser(user: User) {
    this.user.value = user
  }

  fun getUser() = user

  fun isInternetConnected(): Boolean {
    if (!TPUtils.isNetworkAvailable(getContext())) {
      setUserMessage(getString(R.string.error_no_internet))
      return false
    }
    return true
  }

}
