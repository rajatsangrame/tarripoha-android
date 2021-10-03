package com.tarripoha.android.di.component

import com.tarripoha.android.di.WordActivityScope
import com.tarripoha.android.di.module.WordActivityModule
import com.tarripoha.android.ui.word.WordDetailActivity
import dagger.Component

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
@Component(modules = [WordActivityModule::class], dependencies = [ApplicationComponent::class])
@WordActivityScope
interface WordActivityComponent {
    fun injectActivity(activity: WordDetailActivity)
}
