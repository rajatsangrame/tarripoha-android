package com.tarripoha.android.util.helper

import android.app.Application
import com.tarripoha.android.R
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
object PreferenceHelper {

    //region Keys

    const val KEY_USER = "user"
    const val KEY_LOGIN_SKIP = "login_skip"
    const val KEY_NEW_VERSION_AVAILABLE = "new_version_available"
    const val KEY_LAST_UPDATE_CHECK = "last_update_check"

    //endregion

    const val ERROR_NOT_IMPLEMENTED = "Not yet implemented"
    private lateinit var app: Application
    private val pref: SharedPreferences by lazy {
        val context = app.applicationContext
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    }

    fun init(app: Application) {
        this.app = app
    }

    //  NOTE : inline function need public modifiers
    fun getPreferences() = pref

    inline fun <reified T> put(
        key: String,
        value: Any
    ) {
        when (T::class) {
            String::class -> getPreferences().edit { putString(key, value as String) }
            Int::class -> getPreferences().edit { putInt(key, value as Int) }
            Boolean::class -> getPreferences().edit { putBoolean(key, value as Boolean) }
            Float::class -> getPreferences().edit { putFloat(key, value as Float) }
            Long::class -> getPreferences().edit { putLong(key, value as Long) }
            else -> throw UnsupportedOperationException(ERROR_NOT_IMPLEMENTED)
        }
    }

    inline fun <reified T> get(
        key: String,
        default: Any
    ): T {
        return when (T::class) {
            String::class -> getPreferences().getString(key, default as String) as T
            Int::class -> getPreferences().getInt(key, default as Int) as T
            Boolean::class -> getPreferences().getBoolean(key, default as Boolean) as T
            Float::class -> getPreferences().getFloat(key, default as Float) as T
            Long::class -> getPreferences().getLong(key, default as Long) as T
            else -> throw UnsupportedOperationException(ERROR_NOT_IMPLEMENTED)
        }
    }

    fun clear() {
        getPreferences().edit {
            clear()
        }
    }
}
