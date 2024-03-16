package com.tarripoha.android.presentation.login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tarripoha.android.R
import com.tarripoha.android.databinding.FragmentCreateUserBinding
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.ktx.isValidEmail
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateUserFragment : Fragment() {

    // region Variables

    companion object {
        private const val TAG = "CreateUserFragment"
    }

    private lateinit var binding: FragmentCreateUserBinding
    private val viewModel: LoginViewModel by activityViewModels()

    // endregion

    // region Fragment Related Methods

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateUserBinding
            .inflate(LayoutInflater.from(requireContext()), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            actionBtn.setText(R.string.submit)
        }
    }

    private fun setupEditText() {
        binding.nameEt.apply {
            doAfterTextChanged {
                it?.let { _ ->
                    error = null
                }
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    val empty = text.isNullOrEmpty()
                    if (empty) {
                        error = getString(R.string.empty_field)
                    }
                    return@setOnEditorActionListener empty
                }
                true
            }
        }

        binding.emailEt.apply {
            doAfterTextChanged {
                it?.let { _ ->
                    error = null
                }
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // no-op
                    text?.let {
                        val valid = validateData()
                        if (valid) {
                            submitUserInfo()
                        }
                        return@setOnEditorActionListener !valid
                    }
                }
                true
            }
        }
    }

    private fun validateData(): Boolean {
        var isValid = true
        binding.apply {
            if (nameEt.text.isNullOrEmpty()) {
                nameEt.error = getString(R.string.empty_field)
                isValid = false
            } else if (!emailEt.text.isValidEmail()) {
                emailEt.error = getString(R.string.msg_email_not_valid)
                isValid = false
            }
        }
        return isValid
    }

    private fun showKeyboard() {
        Handler(Looper.getMainLooper()).postDelayed({
            TPUtils.showKeyboard(context = requireContext(), view = binding.nameEt)
        }, 500)
    }

    private fun setupObservers() {
        //no-op
    }

    private fun submitUserInfo() {
        viewModel.createUser(
            name = binding.nameEt.text?.trim()
                .toString(),
            email = binding.emailEt.text?.trim()
                .toString()
        )
    }

    // endregion

    // region Click Related Methods

    private fun setupListeners() {
        binding.actionBtn.setOnClickListener {
            if (validateData()) {
                submitUserInfo()
                TPUtils.hideKeyboard(context = requireContext(), view = binding.emailEt)
            }
        }
    }

    // endregion

}
