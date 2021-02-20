package com.tarripoha.android.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tarripoha.android.di.ViewModelKey
import com.tarripoha.android.ui.login.LoginViewModel
import com.tarripoha.android.ui.main.MainViewModel
import com.tarripoha.android.util.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
@Module
abstract class ViewModelModule {

  @Binds
  abstract fun bindViewModelFactory(factory: ViewModelFactory?): ViewModelProvider.Factory?

  @Binds
  @IntoMap
  @ViewModelKey(MainViewModel::class)
  abstract fun provideHomeViewModel(homeViewModel: MainViewModel?): ViewModel?

  @Binds
  @IntoMap
  @ViewModelKey(LoginViewModel::class)
  abstract fun provideLoginViewModel(loginViewModel: LoginViewModel?): ViewModel?

}