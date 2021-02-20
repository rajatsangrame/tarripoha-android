package com.tarripoha.android.di.component

import com.tarripoha.android.data.Repository
import com.tarripoha.android.di.ApplicationScope
import com.tarripoha.android.di.module.ApplicationModule
import com.tarripoha.android.util.ViewModelFactory
import dagger.Component

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
@ApplicationScope
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

    fun getRepository(): Repository

    fun getViewModelFactory(): ViewModelFactory

}