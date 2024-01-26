package com.tarripoha.android

import android.app.Application
import com.tarripoha.android.utils.ReleaseTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        setupTimber()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        else Timber.plant(ReleaseTree())
    }
}
