package com.lilbro.picobotella.ui.autenticacion

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.lilbro.picobotella.R
import com.lilbro.picobotella.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for the user registration screen (HU 2.0).
 *
 * Observes [LoginViewModel.registerState] and [LoginViewModel.isLoadingRegister]
 * to react to registration outcomes without containing any business logic.
 */
@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRealTimeValidation()
        setupButtons()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.isLoadingRegister.observe(viewLifecycleOwner) { isLoading ->
            setLoading(isLoading)
        }

        viewModel.registerState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is LoginViewModel.RegisterResult.Success -> {
                    // HU 3.0: user is already signed in after successful registration
                    findNavController().navigate(R.id.action_register_to_home)
                }
                is LoginViewModel.RegisterResult.Error -> {
                    // HU 2.0, criterion 13: any Firebase error → "Error en el registro"
                    Toast.makeText(context, R.string.error_registro, Toast.LENGTH_SHORT).show()
                }
                is LoginViewModel.RegisterResult.ValidationError -> {
                    // Local validation errors are shown inline, not as toast
                    when (result.code) {
                        "INVALID_EMAIL" -> binding.tilEmail.error = getString(R.string.error_email_invalid)
                        "INVALID_PASSWORD" -> binding.tilPassword.error = getString(R.string.error_password_too_short)
                    }
                }
            }
        }
    }

    private fun setupRealTimeValidation() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateRegisterButtonState()
            }
            override fun afterTextChanged(s: Editable?) {
                val email = binding.tilEmail.editText?.text.toString().trim()
                val password = binding.tilPassword.editText?.text.toString().trim()

                if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.tilEmail.error = getString(R.string.error_email_invalid)
                } else {
                    binding.tilEmail.error = null
                }

                if (password.isNotEmpty() && password.length < 6) {
                    binding.tilPassword.error = getString(R.string.error_password_too_short)
                } else {
                    binding.tilPassword.error = null
                }
            }
        }
        binding.tilEmail.editText?.addTextChangedListener(watcher)
        binding.tilPassword.editText?.addTextChangedListener(watcher)
    }

    private fun updateRegisterButtonState() {
        val email = binding.tilEmail.editText?.text.toString().trim()
        val password = binding.tilPassword.editText?.text.toString().trim()
        binding.btnRegistrar.isEnabled =
            email.isNotEmpty() &&
                    Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                    password.length >= 6
    }

    private fun setupButtons() {
        binding.btnRegistrar.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString().trim()
            val password = binding.tilPassword.editText?.text.toString().trim()
            viewModel.register(email, password)
        }

        binding.tvIrALogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnRegistrar.isEnabled = !isLoading
        binding.tilEmail.isEnabled = !isLoading
        binding.tilPassword.isEnabled = !isLoading
        if (!isLoading) updateRegisterButtonState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}