package com.tarripoha.android.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tarripoha.android.data.Repository
import com.tarripoha.android.ui.main.MainViewModel

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
class ViewModelProviderFactory(private val repository: Repository) : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
      return MainViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}