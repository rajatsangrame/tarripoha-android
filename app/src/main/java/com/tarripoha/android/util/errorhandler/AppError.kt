package com.tarripoha.android.util.errorhandler

import com.tarripoha.android.R
import androidx.annotation.StringRes
import java.net.SocketTimeoutException

sealed class AppError(open val message: Int) {
    data class NetworkError(@StringRes override val message: Int) : AppError(message)
    data class ServerError(@StringRes override val message: Int) : AppError(message)
    data class GenericError(@StringRes override val message: Int) : AppError(message)

    companion object {
        fun parse(cause: Throwable): AppError {
            return when (cause) {
                is SocketTimeoutException -> {
                    NetworkError(R.string.error_timeout)
                }

                else -> GenericError(R.string.error_unknown)
            }
        }
    }
}