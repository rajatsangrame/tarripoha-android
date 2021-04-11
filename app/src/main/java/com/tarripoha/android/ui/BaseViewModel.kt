package com.tarripoha.android.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarripoha.android.util.helper.UserHelper

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

abstract class BaseViewModel(val app: Application) : ViewModel() {

  private val userMessage: MutableLiveData<String> = MutableLiveData()

  fun setUserMessage(msg: String) {
    userMessage.value = msg
  }

  fun getUserMessage() = userMessage

  fun getString(resId: Int) = app.applicationContext.getString(resId)

  fun getContext(): Context = app.applicationContext

  fun getUser() = UserHelper.getUser()

}