package com.tarripoha.android.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.text.format.Time
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tarripoha.android.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
object TPUtils {

  private const val TAG = "TPUtils"

  fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val nw = connectivityManager.activeNetwork ?: return false
      val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
      return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
      }
    } else {
      val nwInfo = connectivityManager.activeNetworkInfo ?: return false
      return nwInfo.isConnected
    }
  }

  fun hideKeyboard(
    context: Context,
    view: View
  ) {
    val inputManager =
      context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(view.windowToken, 0)
  }

  fun showKeyboard(
    context: Context,
    view: View,
  ) {
    val inputManager =
      context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    view.requestFocus()
    inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
  }

  fun showToast(
    context: Context,
    message: String
  ) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT)
        .show()
  }

  fun showSnackBar(
    view: AppCompatActivity,
    message: String
  ) {
    Snackbar.make(view.findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
        .show()
  }

  fun getRandomUuid(length: Int = 16): String {
    check(length <= 32) { "random string length cannot be more than than UUID class limit." }
    val uuid = UUID.randomUUID()
        .toString()
    return uuid.replace("-".toRegex(), "")
        .substring(0, length)
  }

  fun getTime(
    context: Context,
    longTime: Double?
  ): String {
    if (longTime == null) {
      return ""
    }
    var date: Long = longTime.toLong()
    try {
      val compareLong = 12345678901234L
      if (date > compareLong) {
        // micro seconds
        date /= 1000
      }
      var relativeTime: String
      // check if time is under minimum resolution time
      val rightNow = Calendar.getInstance()
      val time = Calendar.getInstance()
      time.timeInMillis = date
      val offset = (rightNow[Calendar.ZONE_OFFSET] +
          rightNow[Calendar.DST_OFFSET]).toLong()
      val millisElapsedToday = (rightNow.timeInMillis + offset) %
          DateUtils.DAY_IN_MILLIS
      relativeTime = DateUtils.getRelativeDateTimeString(
          context, date, DateUtils.MINUTE_IN_MILLIS,
          DateUtils.DAY_IN_MILLIS + millisElapsedToday, DateUtils.FORMAT_ABBREV_ALL
      )
          .toString()
      try {
        when {
          System.currentTimeMillis() - date < millisElapsedToday -> {
            relativeTime = if (DateFormat.is24HourFormat(context)) {
              SimpleDateFormat("H:mm", Locale.getDefault()).format(time.time)
            } else {
              SimpleDateFormat("h:mm a", Locale.getDefault()).format(time.time)
            }
          }
          System.currentTimeMillis() - date < millisElapsedToday + DateUtils.DAY_IN_MILLIS -> {
            // event happened yesterday as difference between current time and event's time since epoch is less than millis elapsed (today + one whole day).
            val timeStrings = relativeTime.split(",".toRegex())
                .toTypedArray()
            timeStrings[0] = context.getString(R.string.yesterday)
            relativeTime = timeStrings[0]
          }
          isSameYear(date) -> {
            relativeTime = SimpleDateFormat("d MMM", Locale.getDefault()).format(date)
          }
          else -> {
            relativeTime = SimpleDateFormat("d MMM yy", Locale.getDefault()).format(date)
          }
        }
      } catch (e: Exception) {
        FirebaseCrashlytics.getInstance()
            .recordException(e)
        Log.e(TAG, "Error while modifying relativeTime. not changing it")
      }
      return relativeTime
    } catch (e: Exception) {
      FirebaseCrashlytics.getInstance()
          .recordException(e)
      Log.e(TAG, "Error while getting relativeTimeString og time :$date")
    }
    return ""
  }

  private fun isSameYear(date: Long): Boolean {
    val time = Time()
    time.set(date)
    val thenYear = time.year
    time.set(System.currentTimeMillis())
    return thenYear == time.year
  }

}