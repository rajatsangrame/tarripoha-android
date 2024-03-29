package com.tarripoha.android.util.helper


import com.tarripoha.android.domain.entity.User
import com.tarripoha.android.util.ktx.toJsonString

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

    fun getName(): String? = user?.name

    fun getPhone(): String? = user?.phone

    fun isLoggedIn(): Boolean = user != null

    fun isLoggedInUser(phone: String): Boolean {
        user?.let {
            return@isLoggedInUser it.phone == phone
        }
        return false
    }

}
