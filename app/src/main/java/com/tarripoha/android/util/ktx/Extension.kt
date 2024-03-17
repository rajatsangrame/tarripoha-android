package com.tarripoha.android.util.ktx

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.Patterns
import android.view.View
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.tarripoha.android.R
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.regex.Pattern

fun CharSequence?.isValidNumber(): Boolean {
    if (isNullOrEmpty()) return false
    return Pattern.matches("[1-9][0-9]{9}", this)
}

fun CharSequence?.isValidEmail(): Boolean {
    if (isNullOrEmpty()) return false
    val pattern = Patterns.EMAIL_ADDRESS
    return pattern.matcher(this)
        .matches()
}

fun View.toggleVisibility(inputText: CharSequence?) {
    visibility = if (inputText.isNullOrEmpty()) View.GONE
    else View.VISIBLE
}

fun View.toggleVisibility(list: List<Any>?, reverse: Boolean = true) {
    // Reverse = TRUE, it works other way. Ex show empty container when the list size is 0
    if (reverse) {
        visibility = if (list.isNullOrEmpty()) View.VISIBLE
        else View.GONE
    } else {
        visibility = if (list.isNullOrEmpty()) View.GONE
        else View.VISIBLE
    }
}

fun TextView.setTextWithVisibility(inputText: CharSequence?) {
    visibility = if (inputText.isNullOrEmpty()) View.GONE
    else {
        text = inputText
        View.VISIBLE
    }
}

fun TextView.underlinedWithVisibility(inputText: String?) {
    visibility = if (inputText.isNullOrEmpty()) View.GONE
    else {
        val htmlText = "<p><u>$inputText</u></p>"
        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY);
        } else Html.fromHtml(htmlText)
        View.VISIBLE
    }
}

fun View.toggleIsEnable(inputText: CharSequence?) {
    isEnabled = !inputText.isNullOrEmpty()
}

fun Any.toJsonString(): String = Gson().toJson(this)

inline fun <reified T> String.parseObject(): T {
    return Gson().fromJson(this, T::class.java)
}

fun CharSequence?.hasEnglishChars(): Boolean {
    if (isNullOrEmpty()) return false
    val p = Pattern.compile("[a-zA-Z0-9]")
    this.forEach {
        if (p.matcher("$it").matches()) {
            return true
        }
    }
    return false
}

// Ref: https://medium.com/over-engineering/hands-on-with-material-components-for-android-dialogs-75c6d726f83a
fun MaterialAlertDialogBuilder.showDialog(
    title: String? = null,
    message: String,
    positiveText: String = context.getString(R.string.yes),
    negativeText: String? = context.getString(R.string.cancel),
    cancelable: Boolean = true,
    positiveListener: () -> Unit,
    negativeListener: () -> Unit
) {
    title?.let {
        this.setTitle(it)
    }
    this
        .setMessage(message)
        .setPositiveButton(positiveText) { dialog, _ ->
            dialog.dismiss()
            positiveListener()
        }
        .setNegativeButton(negativeText) { _, _ ->
            negativeListener()
        }
        .setCancelable(cancelable)
        .show()
}

fun Context.getPackage(): String {
    return this.packageName.replace(".debug", "")
}

