package com.lilbro.picobotella.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that centralizes Firebase Authentication calls: login, register, logout,
 * and current user retrieval. No auth logic should live outside this class.
 */
@Singleton
class AuthRepository @Inject constructor(private val auth: FirebaseAuth) {

    /**
     * Signs in an existing user with email and password.
     *
     * @param email User's email address.
     * @param password User's password.
     * @return A [Task] that resolves to [AuthResult] on success.
     */
    fun login(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    /**
     * Creates a new user account with email and password.
     *
     * @param email User's email address.
     * @param password User's password (minimum 6 characters).
     * @return A [Task] that resolves to [AuthResult] on success.
     */
    fun register(email: String, password: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    /** Returns the currently authenticated [FirebaseUser], or null if not signed in. */
    fun getCurrentUser() = auth.currentUser

    /** Signs out the current user. */
    fun logout() {
        auth.signOut()
    }
}