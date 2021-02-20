package com.tarripoha.android.util

import androidx.test.espresso.idling.CountingIdlingResource
import com.tarripoha.android.BuildConfig

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
object SimpleIdlingResource {

    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        if (BuildConfig.DEBUG) {
            countingIdlingResource.increment()
        }
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow && BuildConfig.DEBUG) {
            countingIdlingResource.decrement()
        }
    }
}