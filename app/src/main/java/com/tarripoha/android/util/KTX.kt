package com.tarripoha.android.util

import android.view.View
import android.widget.TextView
import java.util.regex.Pattern

fun CharSequence.isValidNumber(): Boolean {
  return Pattern.matches("[1-9][0-9]{9}", this)
}

fun View.toggleVisibility(inputText: CharSequence?) {
  visibility = if (inputText.isNullOrEmpty()) View.GONE
  else View.VISIBLE
}

fun View.toggleVisibility(list: List<Any>?) {
  // For List, it works other way. Ex show empty container when the list size is 0
  visibility = if (list.isNullOrEmpty()) View.VISIBLE
  else View.GONE
}

fun TextView.setTextWithVisibility(inputText: CharSequence?) {
  visibility = if (inputText.isNullOrEmpty()) View.GONE
  else {
    text = inputText
    View.VISIBLE
  }
}

fun View.toggleIsEnable(inputText: CharSequence?) {
  isEnabled = !inputText.isNullOrEmpty()
}
