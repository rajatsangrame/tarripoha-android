package com.tarripoha.android.ui.main

import android.util.Log
import androidx.lifecycle.*
import com.tarripoha.android.data.Repository
import javax.inject.Inject

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

class MainViewModel @Inject constructor(var repository: Repository?) : ViewModel() {

    init {
        Log.d(TAG, ": init called")
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}