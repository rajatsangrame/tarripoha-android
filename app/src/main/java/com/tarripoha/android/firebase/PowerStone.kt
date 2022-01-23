package com.tarripoha.android.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tarripoha.android.GlobalVar
import com.tarripoha.android.R
import com.tarripoha.android.data.model.DashboardResponse
import com.tarripoha.android.data.model.FAQResponse
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.toObject
import java.lang.reflect.Type
import java.util.*

object PowerStone {
    private const val TAG = "PowerStone"
    private const val KEY_MINIMUM_VERSION = "min_version"
    private const val KEY_RECOMMENDED_VERSION = "recommended_version"
    private const val KEY_DASHBOARD = "dashboard"
    private const val KEY_DEVANAGARI_CHARS = "devanagari_chars"
    private const val KEY_FAQ = "faq"
    private const val KEY_LANGUAGES = "languages"
    private const val MIN_FETCH_INTERVAL_SEC = 60L

    @JvmStatic
    fun init() {
        Log.i(TAG, "Firebase initialising")
        val remoteConfig = getRemoteConfig()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = MIN_FETCH_INTERVAL_SEC
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    @JvmStatic
    fun fetchData(
        context: Context,
        callback: (message: String, forceUpdate: Boolean) -> Unit
    ) {
        getRemoteConfig().fetchAndActivate()
            .addOnCompleteListener {
                GlobalVar.setCharList(getDevanagariChars())
                val languages: MutableList<String> = getLanguages()
                languages.add(0, context.getString(R.string.select_language))
                GlobalVar.setLanguage(languages)
                var updated = false
                if (it.isSuccessful) {
                    updated = it.result
                }
                Log.i(TAG, "Firebase fetchData: Success. Updated: $updated")
                val appVersion = TPUtils.getAppVersionCode(context)
                checkForUpdate(context, appVersion, callback)
            }
            .addOnFailureListener {
                callback("addOnFailureListener", false)
                Log.e(TAG, "Firebase fetchData: ${it.message}")
            }
            .addOnCanceledListener {
                callback("addOnCanceledListener", false)
                Log.e(TAG, "fetchData: cancelled")
            }
    }

    private fun checkForUpdate(
        context: Context,
        appVersion: Long,
        callback: (String, Boolean) -> Unit
    ) {
        if (appVersion < 1) {
            Log.e(TAG, "checkForUpdate: invalid app version code")
            callback("invalid app version code", false)
            return
        }
        try {
            val minVersion = getRemoteConfig().getString(KEY_MINIMUM_VERSION).toLong()
            val recommendedVersion = getRemoteConfig().getString(KEY_RECOMMENDED_VERSION).toLong()

            var forceUpdate = false
            var message = ""
            if (appVersion < minVersion) {
                //isForceUpdate
                forceUpdate = true
                message = context.getString(R.string.msg_force_update)
            } else if (appVersion < recommendedVersion) {
                forceUpdate = false
                message = context.getString(R.string.msg_update_available)
            }
            callback(message, forceUpdate)
            Log.i(TAG, "checkForUpdate forceUpdate: $forceUpdate")

        } catch (e: Exception) {
            callback(e.toString(), false)
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

    private fun getDevanagariChars(): List<String> {
        val chars = getRemoteConfig().getString(KEY_DEVANAGARI_CHARS)
        return chars.toObject()
    }

    private fun getLanguages(): MutableList<String> {
        val languages = getRemoteConfig().getString(KEY_LANGUAGES)
        return languages.toObject()
    }

    fun getFAQResponse(): Array<FAQResponse> {
        val faqs = getRemoteConfig().getString(KEY_FAQ)
        return Gson().fromJson(faqs, Array<FAQResponse>::class.java)
    }

}
