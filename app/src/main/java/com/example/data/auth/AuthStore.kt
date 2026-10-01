package com.example.data.auth

import android.content.Context
import com.example.data.model.AppMode
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

data class AuthUser(val name: String, val email: String, val role: AppMode, val uid: String)

sealed interface AuthResult {
    data class Success(val user: AuthUser) : AuthResult
    data class Failure(val message: String) : AuthResult
}

class AuthStore(private val context: Context? = null) {
    private val prefs = context?.getSharedPreferences("eatfine_auth_prefs", Context.MODE_PRIVATE)

    private val isFirebaseAvailable: Boolean
        get() = try {
            if (context != null && FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            val app = if (context != null) FirebaseApp.getInstance() else FirebaseApp.getInstance()
            app != null
        } catch (_: Exception) {
            false
        }

    private val auth: FirebaseAuth?
        get() = try {
            if (isFirebaseAvailable) FirebaseAuth.getInstance() else null
        } catch (_: Exception) {
            null
        }

    private val firestore: FirebaseFirestore?
        get() = try {
            if (isFirebaseAvailable) FirebaseFirestore.getInstance() else null
        } catch (_: Exception) {
            null
        }

    fun currentUser(): AuthUser? {
        val firebaseAuth = auth
        if (firebaseAuth != null) {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                try {
                    val firestoreDb = firestore
                    if (firestoreDb != null) {
                        val snapshot = Tasks.await(firestoreDb.collection("users").document(firebaseUser.uid).get())
                        if (snapshot.exists()) {
                            val user = AuthUser(
                                name = snapshot.getString("name").orEmpty().ifBlank { firebaseUser.displayName ?: "EatFine Diner" },
                                email = snapshot.getString("email") ?: firebaseUser.email.orEmpty(),
                                role = roleFromClaims(firebaseUser),
                                uid = firebaseUser.uid
                            )
                            saveLocal(user)
                            return user
                        }
                    }
                } catch (_: Exception) {}

                val cached = getLocal()
                if (cached != null && cached.uid == firebaseUser.uid) return cached
                return AuthUser(
                    name = firebaseUser.displayName ?: "EatFine Diner",
                    email = firebaseUser.email.orEmpty(),
                    role = AppMode.CUSTOMER,
                    uid = firebaseUser.uid
                )
            }
        }
        return getLocal()
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

        val firebaseAuth = auth
        if (firebaseAuth != null) {
            return try {
                val firebaseUser = Tasks.await(firebaseAuth.createUserWithEmailAndPassword(normalizedEmail, password)).user
                    ?: return AuthResult.Failure("Unable to create your account. Please try again.")
                val profile = AuthUser(name.trim(), normalizedEmail, role, firebaseUser.uid)
                try {
                    val firestoreDb = firestore
                    if (firestoreDb != null) {
                        Tasks.await(
                            firestoreDb.collection("users").document(firebaseUser.uid).set(
                                mapOf(
                                    "name" to profile.name,
                                    "email" to profile.email,
                                    "role" to profile.role.name
                                )
                            )
                        )
                    }
                    saveLocal(profile)
                    AuthResult.Success(profile)
                } catch (error: Exception) {
                    runCatching { Tasks.await(firebaseUser.delete()) }
                    firebaseAuth.signOut()
                    throw error
                }
            } catch (error: Exception) {
                AuthResult.Failure(errorMessage(error, registering = true))
            }
        }

        // Local offline fallback
        val localUser = AuthUser(
            name = name.trim(),
            email = normalizedEmail,
            role = role,
            uid = "user_" + UUID.randomUUID().toString().take(8)
        )
        saveLocal(localUser)
        return AuthResult.Success(localUser)
    }

    fun signIn(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
            return AuthResult.Failure("Enter a valid email address.")
        }

        val firebaseAuth = auth
        if (firebaseAuth != null) {
            return try {
                val firebaseUser = Tasks.await(firebaseAuth.signInWithEmailAndPassword(normalizedEmail, password)).user
                    ?: return AuthResult.Failure("Email or password is incorrect.")
                val firestoreDb = firestore
                val snapshot = if (firestoreDb != null) {
                    try {
                        Tasks.await(firestoreDb.collection("users").document(firebaseUser.uid).get())
                    } catch (_: Exception) { null }
                } else null

                val profile = AuthUser(
                    name = snapshot?.getString("name")?.ifBlank { null } ?: firebaseUser.displayName ?: "EatFine Diner",
                    email = snapshot?.getString("email") ?: firebaseUser.email.orEmpty(),
                    role = roleFromClaims(firebaseUser),
                    uid = firebaseUser.uid
                )
                saveLocal(profile)
                AuthResult.Success(profile)
            } catch (error: Exception) {
                AuthResult.Failure(errorMessage(error, registering = false))
            }
        }

        // Local fallback authentication
        val saved = getLocal()
        if (saved != null && saved.email.equals(normalizedEmail, ignoreCase = true)) {
            return AuthResult.Success(saved)
        }
        val user = AuthUser(
            name = normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
            email = normalizedEmail,
            role = AppMode.CUSTOMER,
            uid = "user_" + UUID.randomUUID().toString().take(8)
        )
        saveLocal(user)
        return AuthResult.Success(user)
    }

    fun guestSignIn(): AuthUser {
        val guest = AuthUser(
            name = "Guest Diner",
            email = "guest@eatfine.com",
            role = AppMode.CUSTOMER,
            uid = "guest_user"
        )
        saveLocal(guest)
        return guest
    }

    fun signOut() {
        runCatching { auth?.signOut() }
        prefs?.edit()?.clear()?.apply()
    }

    private fun saveLocal(user: AuthUser) {
        prefs?.edit()?.apply {
            putString("name", user.name)
            putString("email", user.email)
            putString("role", user.role.name)
            putString("uid", user.uid)
            apply()
        }
    }

    private fun getLocal(): AuthUser? {
        val uid = prefs?.getString("uid", null) ?: return null
        val name = prefs.getString("name", "EatFine Diner") ?: "EatFine Diner"
        val email = prefs.getString("email", "diner@eatfine.com") ?: "diner@eatfine.com"
        val roleStr = prefs.getString("role", AppMode.CUSTOMER.name) ?: AppMode.CUSTOMER.name
        val role = runCatching { AppMode.valueOf(roleStr) }.getOrDefault(AppMode.CUSTOMER)
        return AuthUser(name, email, role, uid)
    }

    private fun roleFromClaims(firebaseUser: com.google.firebase.auth.FirebaseUser): AppMode {
        return try {
            val role = Tasks.await(firebaseUser.getIdToken(false)).claims["role"] as? String
            runCatching { AppMode.valueOf(role ?: AppMode.CUSTOMER.name) }.getOrDefault(AppMode.CUSTOMER)
        } catch (_: Exception) {
            AppMode.CUSTOMER
        }
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
