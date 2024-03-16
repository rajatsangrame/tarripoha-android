package com.tarripoha.android.presentation.login

import android.app.Activity
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
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.tarripoha.android.R
import com.tarripoha.android.databinding.LayoutTextInputWithButtonBinding
import com.tarripoha.android.presentation.main.MainActivity
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.helper.LoginHelper
import com.tarripoha.android.util.helper.PreferenceHelper
import com.tarripoha.android.util.ktx.isValidNumber
import com.tarripoha.android.util.ktx.setTextWithVisibility
import com.tarripoha.android.util.ktx.toggleIsEnable
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class LoginFragment : Fragment() {

    // region Variables

    companion object {
        private const val TAG = "LoginFragment"
    }

    private lateinit var binding: LayoutTextInputWithButtonBinding
    private val viewModel: LoginViewModel by activityViewModels()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetLoginParams()
        setupUI()
    }

    // endregion

    // region Helper Methods

    private fun setupUI() {
        setupToolbar()
        setupEditText()
        setupListeners()
        setupObservers()
        showKeyboard()
        binding.apply {
            textInputLayout.hint = getString(R.string.mobile_number)
            optionalTv.setTextWithVisibility(getString(R.string.skip))
        }
    }

    private fun setupToolbar() {
        binding.toolbarLayout.title.text = getString(R.string.login)
        binding.toolbarLayout.btnBack.visibility = View.GONE
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

        viewModel.showProgress.observe(viewLifecycleOwner, Observer {
            it.let {
                if (it == null || !it) {
                    binding.progressBar.visibility = View.GONE
                    val d = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_arrow_forward_white
                    )
                    binding.actionBtn.setImageDrawable(d)
                } else {
                    binding.actionBtn.setImageDrawable(null)
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun showKeyboard() {
        Handler(Looper.getMainLooper()).postDelayed({
            TPUtils.showKeyboard(context = requireContext(), view = binding.inputEt)
        }, 500)
    }

    private fun validateNumber(): Boolean {
        binding.inputEt.text?.let {
            val valid = it.trim()
                .isValidNumber()
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
                processLogin(
                    it.toString()
                        .trim()
                )
                TPUtils.hideKeyboard(context = requireContext(), view = binding.inputEt)
            }
        }
    }

    private fun processLogin(
        number: String,
    ) {
        val phone = "+91$number"
        processLogin(
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
                PreferenceHelper.put<Boolean>(PreferenceHelper.KEY_LOGIN_SKIP, true)
                MainActivity.startMe(requireContext())
                activity?.finish()
            }
        }
    }

    private fun processLogin(
        phone: String,
        activity: Activity,
    ) {
        Timber.tag(TAG).i("processLogin: $phone")
        viewModel.showProgress.value = true
        LoginHelper.processOtpLogin(
            phone = phone,
            activity = activity,
            callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    Timber.tag(TAG).i("onVerificationCompleted: $phone $p0")
                    viewModel.showProgress.value = false
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Timber.tag(TAG).e("onVerificationFailed: $phone ${e.message}")
                    viewModel.showProgress.value = false
                    viewModel.setUserMessage(getString(R.string.error_otp_verification_failed))
                    Timber.tag(TAG).e("onVerificationFailed: $e")
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(id, token)
                    Timber.tag(TAG).i("onCodeSent")
                    viewModel.showProgress.value = false
                    viewModel.phoneNumber = phone
                    viewModel.storedVerificationId = id
                    viewModel.resendToken = token
                    navigateToOtpVerifyFragment()
                }
            }
        )
    }

    // endregion

}
