package com.tarripoha.android.util.helper

import com.tarripoha.android.data.model.User

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
object UserHelper {

  private var user: User? = null

  fun setUser(user: User?) {
    this.user = user
  }

  fun getUser() = user
}