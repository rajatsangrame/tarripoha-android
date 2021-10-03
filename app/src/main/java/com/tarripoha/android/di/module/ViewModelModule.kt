package com.tarripoha.android.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tarripoha.android.di.ViewModelKey
import com.tarripoha.android.ui.login.LoginViewModel
import com.tarripoha.android.ui.main.MainViewModel
import com.tarripoha.android.ui.startup.StartupViewModel
import com.tarripoha.android.ui.word.WordViewModel
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
    abstract fun bindViewModelFactory(viewModel: ViewModelFactory?): ViewModelProvider.Factory?

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun provideHomeViewModel(viewModel: MainViewModel?): ViewModel?

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun provideLoginViewModel(viewModel: LoginViewModel?): ViewModel?

    @Binds
    @IntoMap
    @ViewModelKey(StartupViewModel::class)
    abstract fun provideStartupViewModel(viewModel: StartupViewModel?): ViewModel?

    @Binds
    @IntoMap
    @ViewModelKey(WordViewModel::class)
    abstract fun provideWordDetailViewModel(viewModel: WordViewModel?): ViewModel?

}
