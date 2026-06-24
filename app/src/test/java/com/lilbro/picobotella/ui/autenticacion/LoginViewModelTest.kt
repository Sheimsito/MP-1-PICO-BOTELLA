package com.lilbro.picobotella.ui.autenticacion

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.lilbro.picobotella.data.repository.AuthRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var loginObserver: Observer<LoginViewModel.LoginResult>

    @Mock
    private lateinit var loadingObserver: Observer<Boolean>

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = LoginViewModel(authRepository)
        viewModel.loginState.observeForever(loginObserver)
        viewModel.isLoading.observeForever(loadingObserver)
    }

    @Test
    fun `login success updates state`() {
        val email = "test@test.com"
        val password = "password"
        val mockTask = mock(Task::class.java) as Task<AuthResult>
        
        `when`(authRepository.login(email, password)).thenReturn(mockTask)
        `when`(mockTask.addOnCompleteListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnCompleteListener<AuthResult>
            `when`(mockTask.isSuccessful).thenReturn(true)
            listener.onComplete(mockTask)
            mockTask
        }

        viewModel.login(email, password)

        verify(loadingObserver).onChanged(true)
        verify(loadingObserver).onChanged(false)
        verify(loginObserver).onChanged(LoginViewModel.LoginResult.Success)
    }

    @Test
    fun `login failure updates state with error`() {
        val email = "test@test.com"
        val password = "password"
        val mockTask = mock(Task::class.java) as Task<AuthResult>
        val exception = Exception("Firebase Error")
        
        `when`(authRepository.login(email, password)).thenReturn(mockTask)
        `when`(mockTask.addOnCompleteListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnCompleteListener<AuthResult>
            `when`(mockTask.isSuccessful).thenReturn(false)
            `when`(mockTask.exception).thenReturn(exception)
            listener.onComplete(mockTask)
            mockTask
        }

        viewModel.login(email, password)

        verify(loadingObserver).onChanged(true)
        verify(loadingObserver).onChanged(false)
        verify(loginObserver).onChanged(LoginViewModel.LoginResult.Error("Firebase Error"))
    }
    
    private fun <T> any(): T = org.mockito.ArgumentMatchers.any()
}
