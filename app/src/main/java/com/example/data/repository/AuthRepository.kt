package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

/**
 * Repository coordinating Firebase Authentication and UserProfile persistence.
 * Handles sign-in, registration, sign-out, and resolves the user's role and
 * profile via [UserProfileRepository] upon successful authentication.
 */
class AuthRepository(
    private val userProfileRepository: UserProfileRepository,
    private val context: Context? = null
) {
    private val isFirebaseAvailable: Boolean
        get() = try {
            if (context != null && FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseApp.getApps(context ?: FirebaseApp.getInstance().applicationContext).isNotEmpty()
        } catch (_: Exception) {
            false
        }

    private val auth: FirebaseAuth?
        get() = try {
            if (isFirebaseAvailable) FirebaseAuth.getInstance() else null
        } catch (_: Exception) {
            null
        }

    /**
     * Authenticates an existing user and fetches their corresponding UserProfile
     * from Firestore to determine their platform role.
     */
    suspend fun signIn(email: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        runCatching {
            require(email.isNotBlank()) { "Email cannot be blank" }
            require(password.isNotBlank()) { "Password cannot be blank" }

            val firebaseAuth = auth
            if (firebaseAuth != null) {
                val authResult = Tasks.await(firebaseAuth.signInWithEmailAndPassword(email.trim().lowercase(), password))
                val uid = authResult.user?.uid ?: throw IllegalStateException("Firebase User ID is null after sign in")
                
                // Fetch profile from Firestore
                val profile = userProfileRepository.getUserProfile(uid).getOrNull()
                    ?: UserProfile(
                        userId = uid,
                        email = email.trim().lowercase(),
                        role = UserRole.CUSTOMER.value,
                        name = authResult.user?.displayName.orEmpty()
                    ).also {
                        userProfileRepository.saveUserProfile(it)
                    }
                return@runCatching profile
            }

            // Fallback for offline / demo environments
            UserProfile(
                userId = "local_${email.hashCode()}",
                email = email.trim().lowercase(),
                role = UserRole.CUSTOMER.value,
                name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            )
        }
    }

    /**
     * Registers a new user account with Firebase Auth and creates their
     * initial UserProfile document with customer role in Firestore.
     */
    suspend fun register(name: String, email: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        runCatching {
            require(name.isNotBlank()) { "Name cannot be blank" }
            require(email.isNotBlank() && email.contains("@")) { "Valid email is required" }
            require(password.length >= 8) { "Password must be at least 8 characters" }

            val normalizedEmail = email.trim().lowercase()
            val firebaseAuth = auth

            if (firebaseAuth != null) {
                val authResult = Tasks.await(firebaseAuth.createUserWithEmailAndPassword(normalizedEmail, password))
                val uid = authResult.user?.uid ?: throw IllegalStateException("Firebase User ID is null after registration")
                
                val newProfile = UserProfile(
                    userId = uid,
                    email = normalizedEmail,
                    role = UserRole.CUSTOMER.value,
                    name = name.trim()
                )
                userProfileRepository.saveUserProfile(newProfile)
                return@runCatching newProfile
            }

            // Local fallback
            val localProfile = UserProfile(
                userId = "local_${System.currentTimeMillis()}",
                email = normalizedEmail,
                role = UserRole.CUSTOMER.value,
                name = name.trim()
            )
            userProfileRepository.saveUserProfile(localProfile)
            localProfile
        }
    }

    /**
     * Signs out the current Firebase user.
     */
    fun signOut() {
        auth?.signOut()
    }

    /**
     * Returns the currently authenticated user's ID, or null if signed out.
     */
    val currentUserId: String?
        get() = auth?.currentUser?.uid

    /**
     * Observes authentication state and resolves the current user's profile.
     */
    fun observeCurrentProfile(): Flow<UserProfile?> = callbackFlow {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { fa ->
            val user = fa.currentUser
            if (user == null) {
                trySend(null)
            } else {
                // Launch coroutine to fetch latest profile
                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).runCatching {
                    val profile = kotlinx.coroutines.runBlocking {
                        userProfileRepository.getUserProfile(user.uid).getOrNull()
                    }
                    trySend(profile)
                }
            }
        }

        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }
}
