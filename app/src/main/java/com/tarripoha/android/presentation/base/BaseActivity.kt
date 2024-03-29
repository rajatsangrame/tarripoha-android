package com.tarripoha.android.presentation.base

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tarripoha.android.util.TPUtils

abstract class BaseActivity : AppCompatActivity() {
    fun hideKeyboard(view: View) {
        TPUtils.hideKeyboard(context = this, view = view)
    }
}
