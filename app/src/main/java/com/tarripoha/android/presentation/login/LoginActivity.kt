package com.tarripoha.android.presentation.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.tarripoha.android.R
import com.tarripoha.android.databinding.LayoutToolbarWithNavigationBinding
import com.tarripoha.android.presentation.base.BaseActivity
import com.tarripoha.android.presentation.main.MainActivity
import com.tarripoha.android.util.TPUtils

import com.tarripoha.android.util.helper.UserHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: LayoutToolbarWithNavigationBinding
    private val viewModel: LoginViewModel by viewModels()

    companion object {
        private const val TAG = "LoginActivity"
        fun startMe(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutToolbarWithNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUi()
    }


    private fun setupUi() {
        setupToolbar()
        setupObservers()
        navController = findNavController(R.id.nav_host_fragment)
        navController.setGraph(R.navigation.login_nav_graph)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.apply {
            title = null
            setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun setupObservers() {
        viewModel.getIsUserCreated()
            .observe(this) {
                it?.let {
                    if (it) {
//                    FcmUtil.uploadFCMToken(user = user,
//                        success = {
//                            Log.i(TAG, "onNewToken: uploaded")
//                        }, failure = {
//                            Log.e(TAG, "onNewToken: failed")
//                        })
                        MainActivity.startMe(this)
                        finish()
                    }
                }
            }
        viewModel.getUserMessage()
            .observe(this, Observer {
                TPUtils.showSnackBar(this, it)
            })
    }
}
