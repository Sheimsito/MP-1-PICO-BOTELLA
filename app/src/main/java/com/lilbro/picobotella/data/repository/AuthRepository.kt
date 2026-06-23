package com.lilbro.picobotella.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

// Implementation of AuthRepository class to handle Firebase authentication: login, logout, and current user retrieval.
// IMPLEMENT HERE THE AUTHENTICATION METHODS LIKE REGISTER, FORGOT PASSWORD, ETC.
@Singleton
class AuthRepository @Inject constructor(private val auth: FirebaseAuth) {

    fun login(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun getCurrentUser() = auth.currentUser

    fun logout() {
        auth.signOut()
    }
}
