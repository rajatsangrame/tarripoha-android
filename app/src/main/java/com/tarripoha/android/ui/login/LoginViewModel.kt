package com.tarripoha.android.ui.login

import android.app.Application
import com.tarripoha.android.data.Repository
import com.tarripoha.android.ui.BaseViewModel
import javax.inject.Inject

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

class LoginViewModel @Inject constructor(
  var repository: Repository,
  app: Application
) : BaseViewModel(app) {

  companion object {
    private const val TAG = "LoginViewModel"
  }
}
