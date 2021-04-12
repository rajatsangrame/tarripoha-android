package com.tarripoha.android.di.component

import com.tarripoha.android.di.MainActivityScope
import com.tarripoha.android.di.module.MainActivityModule
import com.tarripoha.android.ui.main.MainActivity
import dagger.Component

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
@Component(modules = [MainActivityModule::class], dependencies = [ApplicationComponent::class])
@MainActivityScope
interface MainActivityComponent {
  fun injectMainActivity(mainActivity: MainActivity)
}
