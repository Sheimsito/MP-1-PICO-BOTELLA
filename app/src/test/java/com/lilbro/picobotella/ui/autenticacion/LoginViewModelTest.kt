package com.lilbro.picobotella.ui.autenticacion

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.lilbro.picobotella.data.repository.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor

/**
 * Unit tests for [LoginViewModel].
 *
 * Uses [InstantTaskExecutorRule] so LiveData updates run synchronously.
 * Firebase [Task] is replaced by [FakeTask] to avoid stubbing final methods
 * from the Play Services SDK, which Mockito cannot mock without native agents.
 */
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        authRepository = mock(AuthRepository::class.java)
        viewModel = LoginViewModel(authRepository)
    }

    // ---------------------------------------------------------------------------
    // FakeTask — replaces Task<AuthResult> mock entirely
    // ---------------------------------------------------------------------------

    /**
     * A simple synchronous fake implementation of [Task] that immediately
     * calls the [OnCompleteListener] upon [addOnCompleteListener].
     *
     * This avoids stubbing final methods on the Play Services [Task] class,
     * which Mockito cannot do without native inline instrumentation.
     *
     * @property successful Whether the operation succeeded.
     * @property error The exception to report on failure; null on success.
     */
    private inner class FakeTask(
        private val successful: Boolean,
        private val error: Exception? = null
    ) : Task<AuthResult>() {

        override fun isComplete(): Boolean = true
        override fun isSuccessful(): Boolean = successful
        override fun isCanceled(): Boolean = false
        override fun getResult(): AuthResult? = null
        override fun <X : Throwable> getResult(exceptionType: Class<X>): AuthResult? = null
        override fun getException(): Exception? = error

        override fun addOnCompleteListener(listener: OnCompleteListener<AuthResult>): Task<AuthResult> {
            listener.onComplete(this)
            return this
        }

        override fun addOnSuccessListener(listener: OnSuccessListener<in AuthResult>): Task<AuthResult> = this
        override fun addOnSuccessListener(executor: Executor, listener: OnSuccessListener<in AuthResult>): Task<AuthResult> = this
        override fun addOnSuccessListener(activity: android.app.Activity, listener: OnSuccessListener<in AuthResult>): Task<AuthResult> = this
        override fun addOnFailureListener(listener: OnFailureListener): Task<AuthResult> = this
        override fun addOnFailureListener(executor: Executor, listener: OnFailureListener): Task<AuthResult> = this
        override fun addOnFailureListener(activity: android.app.Activity, listener: OnFailureListener): Task<AuthResult> = this
        override fun addOnCompleteListener(executor: Executor, listener: OnCompleteListener<AuthResult>): Task<AuthResult> {
            listener.onComplete(this)
            return this
        }
    }

    /** Returns a [FakeTask] that reports success. */
    private fun successTask(): Task<AuthResult> = FakeTask(successful = true)

    /**
     * Returns a [FakeTask] that reports failure with a mocked instance of [exceptionClass].
     *
     * The exception is mocked (not constructed) so that Firebase exceptions whose
     * constructors call [android.text.TextUtils] do not crash in JVM-only tests.
     *
     * @param exceptionClass The class of the exception to simulate.
     */
    private fun <E : Exception> failureTask(exceptionClass: Class<E>): Task<AuthResult> =
        FakeTask(successful = false, error = mock(exceptionClass))

    // ---------------------------------------------------------------------------
    // LOGIN tests
    // ---------------------------------------------------------------------------

    @Test
    fun `login - successful - emits Success and stops loading`() {
        whenever(authRepository.login("user@test.com", "pass123")).thenReturn(successTask())

        viewModel.login("user@test.com", "pass123")

        assertEquals(LoginViewModel.LoginResult.Success, viewModel.loginState.value)
        assertFalse(viewModel.isLoading.value ?: true)
    }

    @Test
    fun `login - calls repository with correct credentials`() {
        whenever(authRepository.login("user@test.com", "pass123")).thenReturn(successTask())

        viewModel.login("user@test.com", "pass123")

        verify(authRepository).login("user@test.com", "pass123")
    }

    @Test
    fun `login - wrong password - emits Error with WRONG_PASSWORD`() {
        whenever(authRepository.login("user@test.com", "wrong"))
            .thenReturn(failureTask(FirebaseAuthInvalidCredentialsException::class.java))

        viewModel.login("user@test.com", "wrong")

        val result = viewModel.loginState.value
        assertTrue(result is LoginViewModel.LoginResult.Error)
        assertEquals("WRONG_PASSWORD", (result as LoginViewModel.LoginResult.Error).code)
    }

    @Test
    fun `login - user not found - emits Error with USER_NOT_FOUND`() {
        whenever(authRepository.login("nobody@test.com", "pass123"))
            .thenReturn(failureTask(FirebaseAuthInvalidUserException::class.java))

        viewModel.login("nobody@test.com", "pass123")

        val result = viewModel.loginState.value
        assertTrue(result is LoginViewModel.LoginResult.Error)
        assertEquals("USER_NOT_FOUND", (result as LoginViewModel.LoginResult.Error).code)
    }

    @Test
    fun `login - network error - emits Error with NETWORK_ERROR`() {
        whenever(authRepository.login("user@test.com", "pass123"))
            .thenReturn(failureTask(FirebaseNetworkException::class.java))

        viewModel.login("user@test.com", "pass123")

        val result = viewModel.loginState.value
        assertTrue(result is LoginViewModel.LoginResult.Error)
        assertEquals("NETWORK_ERROR", (result as LoginViewModel.LoginResult.Error).code)
    }

    // ---------------------------------------------------------------------------
    // REGISTER tests
    // ---------------------------------------------------------------------------

    @Test
    fun `register - valid inputs - calls repository`() {
        whenever(authRepository.register("new@test.com", "pass123")).thenReturn(successTask())

        viewModel.register("new@test.com", "pass123")

        verify(authRepository).register("new@test.com", "pass123")
    }

    @Test
    fun `register - successful - emits Success and stops loading`() {
        whenever(authRepository.register("new@test.com", "pass123")).thenReturn(successTask())

        viewModel.register("new@test.com", "pass123")

        assertEquals(LoginViewModel.RegisterResult.Success, viewModel.registerState.value)
        assertFalse(viewModel.isLoadingRegister.value ?: true)
    }

    @Test
    fun `register - existing email - emits Error with EMAIL_ALREADY_IN_USE`() {
        whenever(authRepository.register("existing@test.com", "pass123"))
            .thenReturn(failureTask(FirebaseAuthUserCollisionException::class.java))

        viewModel.register("existing@test.com", "pass123")

        val result = viewModel.registerState.value
        assertTrue(result is LoginViewModel.RegisterResult.Error)
        assertEquals("EMAIL_ALREADY_IN_USE", (result as LoginViewModel.RegisterResult.Error).code)
    }

    @Test
    fun `register - invalid email format - emits ValidationError without calling repository`() {
        viewModel.register("not-an-email", "pass123")

        val result = viewModel.registerState.value
        assertTrue(result is LoginViewModel.RegisterResult.ValidationError)
        assertEquals("INVALID_EMAIL", (result as LoginViewModel.RegisterResult.ValidationError).code)
        verify(authRepository, never()).register(anyString(), anyString())
    }

    @Test
    fun `register - password too short - emits ValidationError without calling repository`() {
        viewModel.register("valid@test.com", "123")

        val result = viewModel.registerState.value
        assertTrue(result is LoginViewModel.RegisterResult.ValidationError)
        assertEquals("INVALID_PASSWORD", (result as LoginViewModel.RegisterResult.ValidationError).code)
        verify(authRepository, never()).register(anyString(), anyString())
    }

    @Test
    fun `register - network error - emits Error with NETWORK_ERROR`() {
        whenever(authRepository.register("new@test.com", "pass123"))
            .thenReturn(failureTask(FirebaseNetworkException::class.java))

        viewModel.register("new@test.com", "pass123")

        val result = viewModel.registerState.value
        assertTrue(result is LoginViewModel.RegisterResult.Error)
        assertEquals("NETWORK_ERROR", (result as LoginViewModel.RegisterResult.Error).code)
    }
}
