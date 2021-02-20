package com.tarripoha.android.util

import android.content.Context
import com.tarripoha.android.R
import org.json.JSONObject
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 *
 *
 * Util class to handle API Error and show message accordingly
 */
object ApiError {

  /**
   * @return the error text
   */
  fun getErrorMessage(
      context: Context,
      t: Throwable
  ): String {
    return when (t) {
        is UnknownHostException -> {
            context.getString(R.string.error_no_internet)
        }
        is SocketTimeoutException -> {
            context.getString(R.string.error_timeout)
        }
        is HttpException -> {
            try {
                val responseBody = t.response()!!
                    .errorBody()
                val jsonObject = JSONObject(responseBody!!.string())
                jsonObject.getJSONObject("data")
                    .getString("error")
            } catch (ex: Exception) {
                // ignored
                context.getString(R.string.unable_to_fetch)
            }
        }
      else -> {
        context.getString(R.string.unable_to_fetch)
      }
    }
  }
}