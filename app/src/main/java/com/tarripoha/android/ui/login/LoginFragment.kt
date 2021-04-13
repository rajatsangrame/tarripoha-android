package com.tarripoha.android.ui.login

import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.tarripoha.android.TPApp
import com.tarripoha.android.R
import com.tarripoha.android.databinding.LayoutTextInputWithButtonBinding
import com.tarripoha.android.ui.main.MainActivity
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.isValidNumber
import com.tarripoha.android.util.setTextWithVisibility
import com.tarripoha.android.util.toggleIsEnable

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
    viewModel.resetLoginParams()
    setupUI()
  }

  // endregion

  // region Helper Methods

  private fun setupUI() {
    setupEditText()
    setupListeners()
    setupObservers()
    showKeyboard()
    binding.apply {
      textInputLayout.hint = getString(R.string.mobile_number)
      actionBtn.setText(R.string.next)
      optionalTv.setTextWithVisibility(getString(R.string.skip))
    }
  }

  private fun setupEditText() {
    binding.inputEt.apply {
      inputType = InputType.TYPE_CLASS_PHONE
      doAfterTextChanged {
        it?.let { _ ->
          error = null
          binding.actionBtn.toggleIsEnable(it)
        }
      }
      setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          // no-op
          text?.let {
            val valid = validateNumber()
            if (valid) {
              processLogin(it.toString())
            }
            return@setOnEditorActionListener !valid
          }
        }
        true
      }
    }
  }

  private fun setupObservers() {
    viewModel.getIsCodeSent()
        .observe(viewLifecycleOwner, Observer {
          it?.let {
            if (it) navigateToOtpVerifyFragment()
          }
        })
  }

  private fun showKeyboard() {
    Handler().postDelayed({
      TPUtils.showKeyboard(context = requireContext(), view = binding.inputEt)
    }, 500)
  }

  private fun validateNumber(): Boolean {
    binding.inputEt.text?.let {
      val valid = it.trim().isValidNumber()
      if (!valid) {
        binding.inputEt.error = getString(R.string.msg_number_not_valid)
      }
      return valid
    }
    return false
  }

  private fun processLogin() {
    binding.inputEt.text?.let {
      if (validateNumber()) {
        processLogin(it.toString().trim())
        TPUtils.hideKeyboard(context = requireContext(), view = binding.inputEt)
      }
    }
  }

  private fun processLogin(
    number: String,
  ) {
    val phone = "+91$number"
    viewModel.processLogin(
        phone = phone,
        activity = requireActivity()
    )
  }

  private fun navigateToOtpVerifyFragment() {
    findNavController().navigate(R.id.action_LoginFragment_to_OtpVerifyFragment)
  }

  // endregion

  // region Click Related Methods

  private fun setupListeners() {
    binding.apply {
      actionBtn.setOnClickListener {
        processLogin()
      }
      optionalTv.setOnClickListener {
        //Skip
        MainActivity.startMe(requireContext())
        activity?.finish()
      }
    }
  }

  // endregion

}
