package com.tarripoha.android.ui.startup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.tarripoha.android.R
import com.tarripoha.android.TPApp
import com.tarripoha.android.ui.BaseActivity
import com.tarripoha.android.data.model.User
import com.tarripoha.android.databinding.ActivityStartupBinding
import com.tarripoha.android.di.component.DaggerStartupActivityComponent
import com.tarripoha.android.di.component.StartupActivityComponent
import com.tarripoha.android.firebase.PowerStone
import com.tarripoha.android.util.helper.PreferenceHelper
import com.tarripoha.android.util.helper.UserHelper
import com.tarripoha.android.ui.login.LoginActivity
import com.tarripoha.android.ui.login.LoginHelper
import com.tarripoha.android.ui.main.MainActivity
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.ViewModelFactory
import com.tarripoha.android.util.showDialog
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
                PowerStone.recordException(e)
                Log.e(TAG, "onCreate: {${e.message}}")
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            PowerStone.fetchData(context = this, callback = { message, forceUpdate ->
                if (forceUpdate) {
                    showForceUpdateDialog(message)
                } else {
                    if (message == getString(R.string.msg_update_available)) {
                        PreferenceHelper.put<Boolean>(
                            PreferenceHelper.KEY_NEW_VERSION_AVAILABLE,
                            true
                        )
                    } else {
                        PreferenceHelper.put<Boolean>(
                            PreferenceHelper.KEY_NEW_VERSION_AVAILABLE,
                            false
                        )
                    }
                    if (user != null) {
                        viewModel.fetchUserInfo(user!!.phone)
                    } else if (!loginSkip) {
                        LoginActivity.startMe(this)
                        finish()
                    } else {
                        MainActivity.startMe(this)
                        finish()
                    }
                }
            })
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
                        viewModel.setUserMessage(getString(R.string.msg_user_blocked))
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

    private fun showForceUpdateDialog(message: String) {
        MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
            .showDialog(
                title = getString(R.string.update_available),
                message = message,
                positiveText = getString(R.string.update),
                negativeText = getString(R.string.close),
                cancelable = false,
                positiveListener = {
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.data =
                        "https://play.google.com/store/apps/details?id=com.tarripoha.android".toUri()
                    startActivity(intent)
                },
                negativeListener = {
                    finish()
                }
            )
    }

    companion object {
        private const val TAG = "StartupActivity"
    }
}
