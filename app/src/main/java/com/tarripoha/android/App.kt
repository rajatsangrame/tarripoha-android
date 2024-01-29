package com.tarripoha.android

import android.app.Application
import com.tarripoha.android.util.ReleaseTree
import com.tarripoha.android.util.helper.PreferenceHelper
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        setupTimber()
        PreferenceHelper.init(this)
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        else Timber.plant(ReleaseTree())
    }
}
