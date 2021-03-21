package com.tarripoha.android.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
object TPUtils {

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
}