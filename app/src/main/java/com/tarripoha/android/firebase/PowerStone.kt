package com.tarripoha.android.firebase

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tarripoha.android.R
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.showDialog
import java.lang.reflect.Type

object PowerStone {
    private const val TAG = "PowerStone"
    private const val KEY_MINIMUM_VERSION = "min_version"
    private const val KEY_RECOMMENDED_VERSION = "recommended_version"
    private const val KEY_DASHBOARD = "dashboard"
    private const val MIN_FETCH_INTERVAL_SEC = 60L

    @JvmStatic
    fun init(context: Context) {
        Log.i(TAG, "Firebase initialising")
        val remoteConfig = getRemoteConfig()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = MIN_FETCH_INTERVAL_SEC
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    @JvmStatic
    fun checkForUpdate(
        context: Context
    ) {
        getRemoteConfig().fetchAndActivate()
            .addOnCompleteListener {
                var updated = false
                if (it.isSuccessful) {
                    updated = it.result
                }
                Log.i(TAG, "Firebase fetchAndActivate: Success. Updated: $updated")
                val appVersion = TPUtils.getAppVersionName(context)
                checkForUpdate(context, appVersion)
            }
            .addOnFailureListener {
                Log.e(TAG, "Firebase fetchAndActivate: ${it.message}")
            }
            .addOnCanceledListener {
                Log.e(TAG, "fetchAndActivate: cancelled")
            }
    }

    private fun checkForUpdate(
        context: Context,
        appVersion: String,
    ) {
        if (appVersion.isEmpty()) {
            Log.e(TAG, "processUpdate: appVersion not found")
            return
        }
        val minVersion = getRemoteConfig().getString(KEY_MINIMUM_VERSION)
            .replace("[a-zA-Z]|-", "")
        val recommendedVersion = getRemoteConfig().getString(KEY_RECOMMENDED_VERSION)
            .replace("[a-zA-Z]|-", "")
        try {
            val minV: Double = minVersion.toDouble()
            val recommendedV: Double = recommendedVersion.toDouble()
            val appV: Double = appVersion.toDouble()

            var cancelable: Boolean? = null
            var message: String? = null
            var negativeText: String? = context.getString(R.string.cancel)
            if (appV < minV) {
                //isForceUpdate
                cancelable = false
                message = context.getString(R.string.msg_force_update)
                negativeText = null
            } else if (appV < recommendedV) {
                cancelable = true
                message = context.getString(R.string.msg_update_available)
            }
            cancelable?.let {
                MaterialAlertDialogBuilder(context)
                    .showDialog(
                        title = context.getString(R.string.update_available),
                        message = message!!,
                        positiveText = context.getString(R.string.update),
                        negativeText = negativeText,
                        cancelable = cancelable,
                        positiveListener = {
                            val intent = Intent()
                            intent.action = Intent.ACTION_VIEW
                            intent.data =
                                "https://play.google.com/store/apps/details?id=com.tarripoha.android".toUri()
                            context.startActivity(intent)
                        }
                    )
            }
            Log.i(TAG, "processUpdate cancelable: $cancelable")

        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun getRemoteConfig(): FirebaseRemoteConfig {
        val app = FirebaseApp.getInstance()
        return FirebaseRemoteConfig.getInstance(app)
    }

    fun getDashboardInfo(): DashboardResponse {
        val type: Type = object : TypeToken<DashboardResponse>() {}.type
        val info = getRemoteConfig().getString(KEY_DASHBOARD)
        return Gson().fromJson(info, type)
    }

    fun recordException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }
}
