package com.lilbro.picobotella.ui.autenticacion

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.lilbro.picobotella.R
import com.lilbro.picobotella.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRealTimeValidation()
        setupButtons()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            setLoading(isLoading)
        }

        viewModel.loginState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is LoginViewModel.LoginResult.Success -> navigateToHome()
                is LoginViewModel.LoginResult.Error -> {
                    Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupRealTimeValidation() {
        binding.btnLogin.isEnabled = false

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = binding.tilEmail.editText?.text.toString().trim()
                val password = binding.tilPassword.editText?.text.toString().trim()

                binding.btnLogin.isEnabled = email.isNotEmpty() && password.isNotEmpty()

                if (password.isNotEmpty() && password.length < 6) {
                    binding.tilPassword.error = "Mínimo 6 dígitos"
                } else {
                    binding.tilPassword.error = null
                    binding.tilPassword.isErrorEnabled = false
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.tilEmail.editText?.addTextChangedListener(watcher)
        binding.tilPassword.editText?.addTextChangedListener(watcher)
    }

    private fun setupButtons() {
        binding.btnLogin.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString().trim()
            val password = binding.tilPassword.editText?.text.toString().trim()

            if (validateFields(email)) {
                viewModel.login(email, password)
            }
        }

        binding.btnGoogle.setOnClickListener {
            Toast.makeText(context, "TODO: Más tarde.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateFields(email: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "Ingresa tu email"
            return false
        } else if (email.length > 40) {
            binding.tilEmail.error = "El email no puede tener más de 40 caracteres"
            return false
        }
        binding.tilEmail.error = null
        return true
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnLogin.isEnabled = !isLoading
        binding.btnGoogle.isEnabled = !isLoading
        binding.tilEmail.isEnabled = !isLoading
        binding.tilPassword.isEnabled = !isLoading
        
        if (!isLoading) {
            val email = binding.tilEmail.editText?.text.toString().trim()
            val password = binding.tilPassword.editText?.text.toString().trim()
            binding.btnLogin.isEnabled = email.isNotEmpty() && password.isNotEmpty()
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_login_to_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
