package com.tarripoha.android.ui

import com.tarripoha.android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
class BottomSheetMenu : BottomSheetDialogFragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false);
  }
}