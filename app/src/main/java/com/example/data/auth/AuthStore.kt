package com.example.data.auth

import com.example.data.model.AppMode
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

data class AuthUser(val name: String, val email: String, val role: AppMode, val uid: String)

sealed interface AuthResult {
    data class Success(val user: AuthUser) : AuthResult
    data class Failure(val message: String) : AuthResult
}

class AuthStore {
    private val auth = FirebaseAuth.getInstance()
    private val profiles = FirebaseFirestore.getInstance().collection("users")

    fun currentUser(): AuthUser? {
        val firebaseUser = auth.currentUser ?: return null
        val snapshot = Tasks.await(profiles.document(firebaseUser.uid).get())
        if (!snapshot.exists()) {
            auth.signOut()
            return null
        }
        return AuthUser(
            name = snapshot.getString("name").orEmpty(),
            email = snapshot.getString("email") ?: firebaseUser.email.orEmpty(),
            role = roleFromClaims(firebaseUser),
            uid = firebaseUser.uid
        )
    }

    fun register(name: String, email: String, password: String, role: AppMode): AuthResult {
        if (role != AppMode.CUSTOMER) {
            return AuthResult.Failure("Business, delivery, and admin accounts must be provisioned by EatFine.")
        }
        val normalizedEmail = email.trim().lowercase()
        if (name.isBlank() || !normalizedEmail.contains("@")) {
            return AuthResult.Failure("Enter your name and a valid email address.")
        }
        if (password.length < 8) {
            return AuthResult.Failure("Password must be at least 8 characters.")
        }
        return try {
            val firebaseUser = Tasks.await(auth.createUserWithEmailAndPassword(normalizedEmail, password)).user
                ?: return AuthResult.Failure("Unable to create your account. Please try again.")
            val profile = AuthUser(name.trim(), normalizedEmail, role, firebaseUser.uid)
            try {
                Tasks.await(
                    profiles.document(firebaseUser.uid).set(
                        mapOf(
                            "name" to profile.name,
                            "email" to profile.email,
                            "role" to profile.role.name
                        )
                    )
                )
                AuthResult.Success(profile)
            } catch (error: Exception) {
                runCatching { Tasks.await(firebaseUser.delete()) }
                auth.signOut()
                throw error
            }
        } catch (error: Exception) {
            AuthResult.Failure(errorMessage(error, registering = true))
        }
    }

    fun signIn(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        return try {
            val firebaseUser = Tasks.await(auth.signInWithEmailAndPassword(normalizedEmail, password)).user
                ?: return AuthResult.Failure("Email or password is incorrect.")
            val snapshot = Tasks.await(profiles.document(firebaseUser.uid).get())
            if (!snapshot.exists()) {
                auth.signOut()
                return AuthResult.Failure("Account access has not been set up. Contact support.")
            }
            AuthResult.Success(
                AuthUser(
                    name = snapshot.getString("name").orEmpty(),
                    email = snapshot.getString("email") ?: firebaseUser.email.orEmpty(),
                    role = roleFromClaims(firebaseUser),
                    uid = firebaseUser.uid
                )
            )
        } catch (error: Exception) {
            AuthResult.Failure(errorMessage(error, registering = false))
        }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun roleFromClaims(firebaseUser: com.google.firebase.auth.FirebaseUser): AppMode {
        val role = Tasks.await(firebaseUser.getIdToken(true)).claims["role"] as? String
        return runCatching { AppMode.valueOf(role ?: AppMode.CUSTOMER.name) }
            .getOrDefault(AppMode.CUSTOMER)
    }

    private fun errorMessage(error: Exception, registering: Boolean): String {
        val cause = error.cause as? Exception ?: error
        return when (cause) {
            is FirebaseAuthUserCollisionException -> "An account with this email already exists."
            is FirebaseAuthWeakPasswordException -> "Password must be at least 8 characters."
            is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException ->
                if (registering) "Enter a valid email address and password." else "Email or password is incorrect."
            else -> "Unable to ${if (registering) "create your account" else "sign in"}. Check your connection and try again."
        }
    }
}