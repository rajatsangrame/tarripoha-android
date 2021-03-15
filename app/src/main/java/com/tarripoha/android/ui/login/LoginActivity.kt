package com.tarripoha.android.ui.login

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.tarripoha.android.App
import com.tarripoha.android.R
import com.tarripoha.android.databinding.ActivityMainBinding
import com.tarripoha.android.di.component.DaggerLoginActivityComponent
import com.tarripoha.android.di.component.LoginActivityComponent
import com.tarripoha.android.ui.BaseActivity

import com.tarripoha.android.util.ViewModelFactory
import javax.inject.Inject

class LoginActivity : BaseActivity() {

  @Inject
  lateinit var factory: ViewModelFactory

  private lateinit var binding: ActivityMainBinding
  private lateinit var viewModel: LoginViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    getDependency()
    viewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)
  }

  private fun getDependency() {
    val component: LoginActivityComponent = DaggerLoginActivityComponent
        .builder()
        .applicationComponent(
            App.get(this)
                .getComponent()
        )
        .build()
    component.injectLoginActivity(this)
  }
}