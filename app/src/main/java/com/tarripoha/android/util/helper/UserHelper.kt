package com.tarripoha.android.util.helper

import com.tarripoha.android.data.model.User
import com.tarripoha.android.util.toJsonString

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
object UserHelper {

  private var user: User? = null

  fun setUser(user: User?) {
    this.user = user
    user?.let {
      PreferenceHelper.put<String>(PreferenceHelper.KEY_USER, it.toJsonString())
    }
  }

  fun getUser() = user

  fun isLoggedIn(): Boolean = user != null
}