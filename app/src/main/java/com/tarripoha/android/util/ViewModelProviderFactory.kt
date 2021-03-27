package com.tarripoha.android.util

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tarripoha.android.data.Repository
import com.tarripoha.android.ui.main.MainViewModel

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
class ViewModelProviderFactory(
  private val repository: Repository,
  val application: Application
) : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
      return MainViewModel(repository = repository, app = application) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}