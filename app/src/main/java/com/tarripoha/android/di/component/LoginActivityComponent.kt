package com.tarripoha.android.di.component

import com.tarripoha.android.di.MainActivityScope
import com.tarripoha.android.di.module.LoginActivityModule
import com.tarripoha.android.ui.login.LoginActivity
import dagger.Component


/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
@Component(modules = [LoginActivityModule::class], dependencies = [ApplicationComponent::class])
@MainActivityScope
interface LoginActivityComponent {
    fun injectLoginActivity(loginActivity: LoginActivity)
}