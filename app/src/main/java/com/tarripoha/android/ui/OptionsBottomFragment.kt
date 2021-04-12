package com.tarripoha.android.ui

import com.tarripoha.android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tarripoha.android.databinding.FragmentBottomSheetDialogBinding

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
class OptionsBottomFragment : BottomSheetDialogFragment(), View.OnClickListener {

  private var callback: OptionCLickListener? = null
  private lateinit var binding: FragmentBottomSheetDialogBinding

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentBottomSheetDialogBinding
        .inflate(LayoutInflater.from(requireContext()), container, false)
    return binding.root
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheet)
  }

  companion object {
    const val TAG = "OptionsBottomFragment"

    @JvmStatic
    fun newInstance(
      callback: OptionCLickListener,
      bundle: Bundle
    ): OptionsBottomFragment {
      return OptionsBottomFragment().apply {
        this.callback = callback
        arguments = bundle
      }
    }
  }

  override fun onClick(v: View) {
    callback?.let {
      when (v.id) {
        R.id.menu_copy -> {
          it.onClick(Option.Copy)
        }
        R.id.menu_share -> {
          it.onClick(Option.Share)
        }
        R.id.menu_report -> {
          it.onClick(Option.Report)
        }
        R.id.menu_delete -> {
          it.onClick(Option.Delete)
        }
        else -> {
          Log.e(TAG, "onClick: Unknown option selected")
        }
      }
    }
  }

  interface OptionCLickListener {
    fun onClick(option: Option)
  }

  override fun onDestroy() {
    super.onDestroy()
    callback = null
  }

  sealed class Option {
    object Copy : Option()
    object Share : Option()
    object Report : Option()
    object Delete : Option()
  }
}
