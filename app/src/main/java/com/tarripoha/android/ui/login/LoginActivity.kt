package com.tarripoha.android.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.tarripoha.android.TPApp
import com.tarripoha.android.R
import com.tarripoha.android.databinding.LayoutToolbarWithNavigationBinding
import com.tarripoha.android.di.component.DaggerLoginActivityComponent
import com.tarripoha.android.di.component.LoginActivityComponent
import com.tarripoha.android.ui.BaseActivity
import com.tarripoha.android.ui.main.MainActivity
import com.tarripoha.android.util.TPUtils

import com.tarripoha.android.util.ViewModelFactory
import com.tarripoha.android.util.helper.UserHelper
import javax.inject.Inject

class LoginActivity : BaseActivity() {

  @Inject
  lateinit var factory: ViewModelFactory
  private lateinit var navController: NavController

  private lateinit var binding: LayoutToolbarWithNavigationBinding
  private lateinit var viewModel: LoginViewModel

  companion object {
    fun startMe(context: Context) {
      context.startActivity(Intent(context, LoginActivity::class.java))
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = LayoutToolbarWithNavigationBinding.inflate(layoutInflater)
    setContentView(binding.root)
    getDependency()
    viewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)
    setupUi()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        hideKeyboard(binding.root)
        super.onBackPressed()
      }
    }
    return true
  }

  private fun getDependency() {
    val component: LoginActivityComponent = DaggerLoginActivityComponent
        .builder()
        .applicationComponent(
            TPApp.get(this)
                .getComponent()
        )
        .build()
    component.injectLoginActivity(this)
  }

  private fun setupUi() {
    setupToolbar()
    setupObservers()
    navController = findNavController(R.id.nav_host_fragment)
    navController.setGraph(R.navigation.login_nav_graph)
    handleNavigation()
  }

  private fun setupToolbar() {
    setSupportActionBar(binding.toolbarLayout.toolbar)
    supportActionBar?.apply {
      title = null
      setDisplayHomeAsUpEnabled(false)
    }
  }

  private fun handleNavigation() {
    navController.addOnDestinationChangedListener { _, destination, _ ->
      when (destination.id) {
        R.id.nav_login -> {
          supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            binding.toolbarLayout.apply {
              title.text = getString(R.string.login)
            }
          }
        }
        R.id.nav_otp_verify -> {
          supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
            binding.toolbarLayout.apply {
              title.text = getString(R.string.verify_otp)
            }
          }
        }
        R.id.nav_create_user -> {
          supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            binding.toolbarLayout.apply {
              title.text = getString(R.string.almost_done)
            }
          }
        }
      }
    }
  }

  private fun setupObservers() {
    viewModel.getUser()
        .observe(this, Observer { user ->
          user?.let {
            UserHelper.setUser(it)
            MainActivity.startMe(this)
            finish()
          }
        })
    viewModel.getUserMessage()
        .observe(this, Observer {
          TPUtils.showSnackBar(this, it)
        })
  }
}
