package com.tarripoha.android.ui.login

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import com.tarripoha.android.TPApp
import com.tarripoha.android.R
import com.tarripoha.android.databinding.LayoutTextInputWithButtonBinding
import com.tarripoha.android.util.TPUtils

class LoginFragment : Fragment() {

  // region Variables

  companion object {
    private const val TAG = "LoginFragment"
  }

  private lateinit var factory: ViewModelProvider.Factory
  private lateinit var binding: LayoutTextInputWithButtonBinding
  private val viewModel by activityViewModels<LoginViewModel> {
    factory
  }

  // endregion

  // region Fragment Related Methods

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = LayoutTextInputWithButtonBinding
        .inflate(LayoutInflater.from(requireContext()), container, false)
    return binding.root
  }

  /**
   * Called when fragment's activity is created.
   * 1. Setup UI for the activity. See [setupUI].
   *
   * @param savedInstanceState Saved data on config or state change.
   */
  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    factory =
      ViewModelProvider.AndroidViewModelFactory(TPApp.get(requireContext()))

    setupUI()
  }

  // endregion

  // region Helper Methods

  private fun setupUI() {
    binding.inputEt.apply {
      hint = getString(R.string.mobile_number)
      inputType = InputType.TYPE_CLASS_PHONE
      doAfterTextChanged {
        it?.let { _ ->

        }
      }
    }
    binding.apply {
      actionBtn.setText(R.string.next)
      TPUtils.handleViewVisibility(optionalTv, getString(R.string.skip))
    }
    setupListeners()
    setupObservers()
  }

  private fun setupObservers() {
    // no-op
  }

  // endregion

  // region Click Related Methods

  private fun setupListeners() {
    binding.apply {
      actionBtn.setOnClickListener {

      }
      optionalTv.setOnClickListener {

      }
    }
  }

  // endregion

}
