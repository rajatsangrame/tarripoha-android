package com.tarripoha.android.di.component

import com.tarripoha.android.di.WordActivityScope
import com.tarripoha.android.di.module.WordDetailActivityModule
import com.tarripoha.android.ui.word.WordDetailActivity
import dagger.Component

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
@Component(
    modules = [WordDetailActivityModule::class],
    dependencies = [ApplicationComponent::class]
)
@WordActivityScope
interface WordDetailActivityComponent {
    fun injectActivity(activity: WordDetailActivity)
}
