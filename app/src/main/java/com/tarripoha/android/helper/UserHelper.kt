package com.tarripoha.android.helper

import android.app.Application
import com.tarripoha.android.R
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
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