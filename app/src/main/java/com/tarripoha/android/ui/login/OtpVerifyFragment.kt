package com.tarripoha.android.ui.login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.tarripoha.android.TPApp
import com.tarripoha.android.R
import com.tarripoha.android.databinding.LayoutTextInputWithButtonBinding
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.toggleIsEnable

class OtpVerifyFragment : Fragment() {

    // region Variables

    companion object {
        private const val TAG = "OtpVerifyFragment"
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
        setupEditText()
        setupListeners()
        setupObservers()
        showKeyboard()
        binding.apply {
            textInputLayout.hint = getString(R.string.otp)
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
                    text?.let {
                        val valid = validateOtp()
                        if (valid) {
                            viewModel.verifyOtp(it.toString(), requireActivity())
                        }
                        return@setOnEditorActionListener !valid
                    }
                }
                true
            }
        }
    }

    private fun validateOtp(): Boolean {
        binding.inputEt.text.let {
            val valid = !it.isNullOrEmpty()
            if (!valid) {
                binding.inputEt.error = getString(R.string.msg_number_not_valid)
            }
            return valid
        }
    }

    private fun showKeyboard() {
        Handler(Looper.getMainLooper()).postDelayed({
            TPUtils.showKeyboard(context = requireContext(), view = binding.inputEt)
        }, 500)
    }

    private fun setupObservers() {
        viewModel.getIsNewUserCreated()
            .observe(viewLifecycleOwner, Observer {
                it?.let {
                    if (it) navigateToCreateUserFragment()
                }
            })
        viewModel.getIsDirtyAccount()
            .observe(viewLifecycleOwner, Observer {
                it?.let {
                    if (it) findNavController().popBackStack()
                }
            })
        viewModel.getShowProgress()
            .observe(viewLifecycleOwner, Observer {
                it.let {
                    if (it == null || !it) {
                        binding.progressBar.visibility = View.GONE
                        val d = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_arrow_forward_white
                        )
                        binding.actionBtn.setImageDrawable(d)
                    } else {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.actionBtn.setImageDrawable(null)
                    }
                }
            })
    }

    private fun navigateToCreateUserFragment() {
        findNavController().navigate(R.id.action_OtpVerifyFragment_to_CreateUserFragment)
    }

    // endregion

    // region Click Related Methods

    private fun setupListeners() {
        binding.actionBtn.setOnClickListener {
            binding.inputEt.text?.let {
                if (validateOtp()) {
                    viewModel.verifyOtp(
                        it.toString()
                            .trim(), requireActivity()
                    )
                    TPUtils.hideKeyboard(context = requireContext(), view = binding.inputEt)
                }
            }
        }
    }

    // endregion

}
