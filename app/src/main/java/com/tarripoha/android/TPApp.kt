package com.tarripoha.android

import android.app.Application
import android.content.Context
import com.tarripoha.android.di.component.ApplicationComponent
import com.tarripoha.android.di.component.DaggerApplicationComponent
import com.tarripoha.android.di.module.ApplicationModule
import com.tarripoha.android.di.module.ContextModule
import com.tarripoha.android.firebase.PowerStone
import com.tarripoha.android.util.helper.PreferenceHelper

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
class TPApp : Application() {

    private var component: ApplicationComponent? = null

    override fun onCreate() {
        super.onCreate()
        component = DaggerApplicationComponent
            .builder()
            .applicationModule(ApplicationModule(application = this))
            .contextModule(ContextModule(context = this))
            .build()
        PowerStone.init()
        PreferenceHelper.init(this)
    }

    fun getComponent(): ApplicationComponent? {
        return component
    }

    companion object {
        @JvmStatic
        fun get(context: Context): TPApp {
            return context.applicationContext as TPApp
        }
    }
}
