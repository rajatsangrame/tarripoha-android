package com.tarripoha.android.ui

import com.tarripoha.android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    setupOptions()
    setupListeners()
  }

  override fun onDestroy() {
    super.onDestroy()
    callback = null
  }

  private fun setupOptions() {
    arguments?.let {
      if (it.containsKey(KEY_OPTIONS)) {
        val options = it.getString(KEY_OPTIONS)
        val type = object : TypeToken<List<Option>>() {}.type
        val gson = Gson()
        val optionList = gson.fromJson<List<Option>>(options, type)
        setupOptions(optionList)
      }
    }
  }

  private fun setupOptions(optionList: List<Option>) {
    optionList.let {
      if (it.contains(Option.Edit)) {
        binding.menuEdit.visibility = View.VISIBLE
      }
      if (it.contains(Option.Delete)) {
        binding.menuDelete.visibility = View.VISIBLE
      }
    }
  }

  override fun onClick(v: View) {
    callback?.let {
      when (v.id) {
        R.id.menu_copy -> {
          it.onClick(Option.Copy)
        }
        R.id.menu_edit -> {
          it.onClick(Option.Edit)
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
      dismiss()
    }
  }

  private fun setupListeners() {
    binding.apply {
      menuCopy.setOnClickListener(this@OptionsBottomFragment)
      menuEdit.setOnClickListener(this@OptionsBottomFragment)
      menuShare.setOnClickListener(this@OptionsBottomFragment)
      menuReport.setOnClickListener(this@OptionsBottomFragment)
      menuDelete.setOnClickListener(this@OptionsBottomFragment)
    }
  }

  interface OptionCLickListener {
    fun onClick(option: Option)
  }

  enum class Option {
    Copy,
    Edit,
    Share,
    Report,
    Delete
  }

  companion object {
    const val TAG = "OptionsBottomFragment"
    const val KEY_OPTIONS = "options"

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
}
