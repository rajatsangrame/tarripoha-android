package com.tarripoha.android.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.Resources.getSystem
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.text.format.Time
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.tarripoha.android.GlobalVar
import com.tarripoha.android.R
import timber.log.Timber
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.floor
import kotlin.math.log10

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
object TPUtils {

    private const val TAG = "TPUtils"

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
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
                        relativeTime =
                            SimpleDateFormat("d MMM yy", Locale.getDefault()).format(date)
                    }
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error while modifying relativeTime. not changing it")
            }
            return relativeTime
        } catch (e: Exception) {
            Timber.tag(TAG).e("Error while getting relativeTimeString og time :$date")
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

    /**
     * @param context App's context
     * @return Application version name
     */
    fun getAppVersionName(context: Context): String {
        return try {
            val info: PackageInfo = context.packageManager
                .getPackageInfo(context.packageName, 0)
            var result: String = info.versionName
            result = result.replace("[a-zA-Z]|-", "")
            result
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * @param context App's context
     * @return Application version code
     */
    fun getAppVersionCode(context: Context): Long {
        val info: PackageInfo = context.packageManager
            .getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            info.versionCode.toLong()
        }
    }

    fun prettyCount(number: Number): String {
        val suffix = charArrayOf(' ', 'K', 'M', 'B', 'T', 'P', 'E')
        val numValue = number.toLong()
        val value = floor(log10(numValue.toDouble())).toInt()
        val base = value / 3
        return if (value >= 3 && base < suffix.size) {
            DecimalFormat("#0.0").format(
                numValue / Math.pow(
                    10.0,
                    (base * 3).toDouble()
                )
            ) + suffix[base]
        } else {
            DecimalFormat("#,##0").format(numValue)
        }
    }

    fun showTotalLikes(likes: MutableMap<String, Boolean>?, view: TextView) {
        if (likes.isNullOrEmpty()) {
            view.visibility = View.GONE
            return
        }
        val context = view.context
        var count = 0
        likes.forEach {
            if (it.value) {
                count++
            }
        }
        when (count) {
            0 -> {
                view.visibility = View.GONE
            }

            1 -> {
                view.text = context.getString(R.string.like, prettyCount(count))
                view.visibility = View.VISIBLE
            }

            else -> {
                view.text = context.getString(R.string.likes, prettyCount(count))
                view.visibility = View.VISIBLE
            }
        }
    }

    fun navigateToPlayStore(context: Context, appId: String) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appId")
                )
            )
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appId")
                )
            )
        }
    }

    val Int.toDp: Int get() = (this / getSystem().displayMetrics.density).toInt()

    val Int.toPx: Int get() = (this * getSystem().displayMetrics.density).toInt()

}