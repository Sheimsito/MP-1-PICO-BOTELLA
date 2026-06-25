package com.lilbro.picobotella.ui.autenticacion

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.lilbro.picobotella.R
import com.lilbro.picobotella.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    private val webClientId = "633148457442-d86j8duns1hb922n8cg398d32g4k8ld2.apps.googleusercontent.com"

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
                    val errorMessage = when (result.code) {
                        "USER_NOT_FOUND" -> getString(R.string.error_user_not_found)
                        "WRONG_PASSWORD" -> getString(R.string.error_wrong_password)
                        "NETWORK_ERROR" -> getString(R.string.error_network)
                        "GOOGLE_ERROR" -> getString(R.string.error_google_signin)
                        else -> getString(R.string.error_auth_failed) + ": " + result.code
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupRealTimeValidation() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonState()
            }
            override fun afterTextChanged(s: Editable?) {
                val email = binding.tilEmail.editText?.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
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
        binding.etPassword.addTextChangedListener(watcher)
    }

    private fun updateLoginButtonState() {
        val email = binding.tilEmail.editText?.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        binding.btnLogin.isEnabled =
            email.isNotEmpty() && password.isNotEmpty() && password.length >= 6
    }

    private fun setupButtons() {
        binding.btnLogin.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (validateFields(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.btnGoogle.setOnClickListener {
            launchGoogleSignIn()
        }

        binding.tvRegistrarse.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }

    private fun launchGoogleSignIn() {
        val credentialManager = CredentialManager.create(requireContext())

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireActivity()
                )
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.loginWithGoogle(googleCredential.idToken)
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.error_google_signin),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: GetCredentialException) {
                Log.e("GoogleSignIn", "Error: ${e.javaClass.simpleName} - ${e.message}", e)
                Toast.makeText(
                    context,
                    "Google error: ${e.javaClass.simpleName}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun validateFields(email: String, password: String): Boolean {
        var isValid = true
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_empty)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_email_invalid)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_empty)
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_too_short)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }
        return isValid
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnLogin.isEnabled = !isLoading
        binding.btnGoogle.isEnabled = !isLoading
        binding.tilEmail.isEnabled = !isLoading
        binding.tilPassword.isEnabled = !isLoading
        binding.tvRegistrarse.isEnabled = !isLoading
        if (!isLoading) updateLoginButtonState()
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_login_to_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}