package com.tarripoha.android.di.component

import com.tarripoha.android.di.StartupActivityScope
import com.tarripoha.android.di.module.StartupActivityModule
import com.tarripoha.android.ui.startup.StartupActivity
import dagger.Component

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
@Component(modules = [StartupActivityModule::class], dependencies = [ApplicationComponent::class])
@StartupActivityScope
interface StartupActivityComponent {
    fun injectActivity(activity: StartupActivity)
}
