package com.lilbro.picobotella.ui.autenticacion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.lilbro.picobotella.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginResult>()
    val loginState: LiveData<LoginResult> = _loginState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

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

    sealed class LoginResult {
        object Success : LoginResult()
        data class Error(val code: String) : LoginResult()
    }
}
