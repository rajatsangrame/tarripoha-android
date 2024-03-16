package com.tarripoha.android.util.errorhandler

import androidx.annotation.StringRes

sealed class Result<out T : Any> {
    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(@StringRes val message: Int, val cause: AppError? = null) : Result<Nothing>()
}