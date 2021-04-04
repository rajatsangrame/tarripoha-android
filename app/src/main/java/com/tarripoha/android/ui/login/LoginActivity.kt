package com.tarripoha.android.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.tarripoha.android.TPApp
import com.tarripoha.android.R
import com.tarripoha.android.databinding.LayoutToolbarWithNavigationBinding
import com.tarripoha.android.di.component.DaggerLoginActivityComponent
import com.tarripoha.android.di.component.LoginActivityComponent
import com.tarripoha.android.ui.BaseActivity

import com.tarripoha.android.util.ViewModelFactory
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
    navController = findNavController(R.id.nav_host_fragment)
    navController.setGraph(R.navigation.login_nav_graph)
    handleNavigation()
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

  private fun handleNavigation() {
    navController.addOnDestinationChangedListener { _, destination, _ ->
      when (destination.id) {
      }
    }
  }
}
