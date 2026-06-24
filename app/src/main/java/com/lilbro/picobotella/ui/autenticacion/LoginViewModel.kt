package com.lilbro.picobotella.ui.autenticacion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.lilbro.picobotella.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the login and registration screen.
 *
 * Handles input validation and delegates authentication to [AuthRepository].
 * Uses a pure Kotlin Regex for email validation so that local JVM unit tests
 * work without the Android runtime (android.util.Patterns is null in JVM tests).
 *
 * @property authRepository Repository that wraps Firebase Auth calls.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    /** Regex for basic email format validation. Does not depend on android.util.Patterns. */
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // --- Login state ---
    private val _loginState = MutableLiveData<LoginResult>()
    val loginState: LiveData<LoginResult> = _loginState

    /** Loading state for login operations. Used by loginFragment. */
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // --- Register state ---
    private val _registerState = MutableLiveData<RegisterResult>()
    val registerState: LiveData<RegisterResult> = _registerState

    /** Loading state for register operations. Used by RegisterFragment. */
    private val _isLoadingRegister = MutableLiveData<Boolean>()
    val isLoadingRegister: LiveData<Boolean> = _isLoadingRegister

    /**
     * Attempts to sign in the user with the provided credentials.
     *
     * @param email The user's email address.
     * @param password The user's password.
     */
    fun login(email: String, password: String) {
        _isLoading.value = true
        authRepository.login(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _loginState.value = LoginResult.Success
                } else {
                    val errorCode = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "USER_NOT_FOUND"
                        is FirebaseAuthInvalidCredentialsException -> "WRONG_PASSWORD"
                        is FirebaseNetworkException -> "NETWORK_ERROR"
                        else -> task.exception?.message ?: "UNKNOWN"
                    }
                    _loginState.value = LoginResult.Error(errorCode)
                }
            }
    }

    /**
     * Attempts to register a new user with the provided credentials.
     *
     * Validates inputs locally before calling the repository.
     * Emits [RegisterResult.ValidationError] for invalid inputs,
     * [RegisterResult.Success] on success, or [RegisterResult.Error] on failure.
     *
     * @param email The desired email address.
     * @param password The desired password (minimum 6 characters).
     */
    fun register(email: String, password: String) {
        if (email.isBlank() || !emailRegex.matches(email)) {
            _registerState.value = RegisterResult.ValidationError("INVALID_EMAIL")
            return
        }
        if (password.isBlank() || password.length < 6) {
            _registerState.value = RegisterResult.ValidationError("INVALID_PASSWORD")
            return
        }

        _isLoadingRegister.value = true
        authRepository.register(email, password)
            .addOnCompleteListener { task ->
                _isLoadingRegister.value = false
                if (task.isSuccessful) {
                    _registerState.value = RegisterResult.Success
                } else {
                    val errorCode = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> "EMAIL_ALREADY_IN_USE"
                        is FirebaseAuthInvalidCredentialsException -> "INVALID_EMAIL"
                        is FirebaseNetworkException -> "NETWORK_ERROR"
                        else -> task.exception?.message ?: "UNKNOWN"
                    }
                    _registerState.value = RegisterResult.Error(errorCode)
                }
            }
    }

    sealed class LoginResult {
        object Success : LoginResult()
        data class Error(val code: String) : LoginResult()
    }

    sealed class RegisterResult {
        object Success : RegisterResult()
        data class Error(val code: String) : RegisterResult()
        data class ValidationError(val code: String) : RegisterResult()
    }
}