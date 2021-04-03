package com.tarripoha.android.ui.startup

import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.tarripoha.android.ui.BaseActivity
import com.tarripoha.android.data.model.User
import com.tarripoha.android.databinding.ActivityStartupBinding
import com.tarripoha.android.helper.PreferenceHelper
import com.tarripoha.android.helper.UserHelper
import com.tarripoha.android.ui.main.MainActivity
import java.lang.Exception

class StartupActivity : BaseActivity() {
  private lateinit var binding: ActivityStartupBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityStartupBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val userString: String? = PreferenceHelper.get<String>(PreferenceHelper.KEY_USER, "")
    var user: User? = null
    userString?.let {
      try {
        user = Gson().fromJson(userString, User::class.java)
      } catch (e: Exception) {
        FirebaseCrashlytics.getInstance()
            .recordException(e)
        Log.e(TAG, "onCreate: {${e.message}}")
      }
    }

    Handler().postDelayed({
      if (user != null) {
        UserHelper.setUser(user = user)
      } else {
        Log.i(TAG, "onCreate: user is null")
      }
      MainActivity.startMe(this)
      finish()
    }, 2000)
  }

  companion object {
    private const val TAG = "StartupActivity"
  }
}
