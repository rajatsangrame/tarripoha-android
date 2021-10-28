package com.tarripoha.android.ui.startup

import android.app.Application
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.tarripoha.android.R
import com.tarripoha.android.data.Repository
import com.tarripoha.android.data.model.User
import com.tarripoha.android.ui.BaseViewModel
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

class StartupViewModel @Inject constructor(
    private val repository: Repository,
    app: Application
) : BaseViewModel(app) {

    fun fetchUserInfo(phoneNumber: String) {
        if (!checkNetworkAndShowError()) {
            this.setUser(null)
            return
        }
        Log.i(TAG, "fetchUserInfo: ")
        repository.fetchUserInfo(
            phone = phoneNumber, success = {
                fetchUserInfoResponse(it)
            }, failure = {
                this.setUser(null)
                setUserMessage(getString(R.string.error_unable_to_fetch))
            }, connectionStatus = {
                if (!it) {
                    setShowProgress(false)
                }
            })
    }

    private fun fetchUserInfoResponse(
        snapshot: DataSnapshot,
    ) {
        Log.i(TAG, "fetchUserInfoResponse: ")
        try {
            if (snapshot.childrenCount > 0) {
                if (snapshot.getValue(User::class.java) != null
                ) {
                    val user: User = snapshot.getValue(User::class.java)!!
                    Log.i(TAG, "fetchUserInfoResponse: user found ${user.phone}")
                    this.setUser(user)
                }
            } else {
                Log.e(TAG, "fetchAllResponse: user not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAllResponse: ${e.localizedMessage}")
        }
    }

    companion object {
        private const val TAG = "StartupViewModel"
    }
}
