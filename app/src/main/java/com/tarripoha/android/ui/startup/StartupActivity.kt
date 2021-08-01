package com.tarripoha.android.ui.startup

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.tarripoha.android.TPApp
import com.tarripoha.android.ui.BaseActivity
import com.tarripoha.android.data.model.User
import com.tarripoha.android.databinding.ActivityStartupBinding
import com.tarripoha.android.di.component.DaggerStartupActivityComponent
import com.tarripoha.android.di.component.StartupActivityComponent
import com.tarripoha.android.util.helper.PreferenceHelper
import com.tarripoha.android.util.helper.UserHelper
import com.tarripoha.android.ui.login.LoginActivity
import com.tarripoha.android.ui.login.LoginHelper
import com.tarripoha.android.ui.main.MainActivity
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.ViewModelFactory
import java.lang.Exception
import javax.inject.Inject

class StartupActivity : BaseActivity() {

    @Inject
    lateinit var factory: ViewModelFactory
    private lateinit var binding: ActivityStartupBinding
    private lateinit var viewModel: StartupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getDependency()
        viewModel = ViewModelProvider(this, factory).get(StartupViewModel::class.java)
        setupObservers()

        val userString: String = PreferenceHelper.get<String>(PreferenceHelper.KEY_USER, "")
        val loginSkip: Boolean =
            PreferenceHelper.get<Boolean>(PreferenceHelper.KEY_LOGIN_SKIP, false)

        var user: User? = null
        userString.let {
            try {
                user = Gson().fromJson(userString, User::class.java)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance()
                    .recordException(e)
                Log.e(TAG, "onCreate: {${e.message}}")
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (user != null) {
                viewModel.fetchUserInfo(user!!.phone)
            } else if (!loginSkip) {
                LoginActivity.startMe(this)
                finish()
            } else {
                MainActivity.startMe(this)
                finish()
            }
        }, 1000)

    }

    private fun getDependency() {
        val component: StartupActivityComponent = DaggerStartupActivityComponent
            .builder()
            .applicationComponent(
                TPApp.get(this)
                    .getComponent()
            )
            .build()
        component.injectActivity(this)
    }

    private fun setupObservers() {
        viewModel.getUser()
            .observe(this, Observer { user ->
                user?.let {
                    val dirty = it.dirty
                    if (dirty != null && dirty) {
                        LoginHelper.logoutUser()
                        PreferenceHelper.clear()
                        LoginActivity.startMe(this)
                        finish()
                        return@Observer
                    } else {
                        UserHelper.setUser(it)
                    }
                }
                MainActivity.startMe(this)
                finish()
            })
        viewModel.getUserMessage()
            .observe(this, Observer {
                TPUtils.showToast(this, it)
            })
    }

    companion object {
        private const val TAG = "StartupActivity"
    }
}
