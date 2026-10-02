package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthResult
import com.example.data.auth.AuthStore
import com.example.data.model.AppMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthAndRoleSecurityTest {

    private lateinit var context: Context
    private lateinit var authStore: AuthStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear preferences before each test
        context.getSharedPreferences("eatfine_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        authStore = AuthStore(context)
    }

    @Test
    fun `test public registration creates Customer account`() {
        val result = authStore.register(
            name = "Sarah Connor",
            email = "sarah.connor@example.com",
            password = "securePassword123",
            role = AppMode.CUSTOMER
        )

        assertTrue("Registration should succeed", result is AuthResult.Success)
        val user = (result as AuthResult.Success).user
        assertEquals("Sarah Connor", user.name)
        assertEquals("sarah.connor@example.com", user.email)
        assertEquals(AppMode.CUSTOMER, user.role)
        assertTrue(user.uid.isNotBlank())
    }

    @Test
    fun `test public registration blocks Admin self-registration`() {
        val result = authStore.register(
            name = "Hacker",
            email = "hacker@example.com",
            password = "securePassword123",
            role = AppMode.ADMIN
        )

        assertTrue("Admin self-registration must be blocked", result is AuthResult.Failure)
        val failure = result as AuthResult.Failure
        assertTrue(
            "Error message should mention provisioning",
            failure.message.contains("provisioned", ignoreCase = true)
        )
    }

    @Test
    fun `test public registration blocks Partner self-registration`() {
        val businessResult = authStore.register(
            name = "Fake Partner",
            email = "fake@partner.com",
            password = "securePassword123",
            role = AppMode.RESTAURANT_PARTNER
        )
        assertTrue("Business partner self-registration must be blocked", businessResult is AuthResult.Failure)

        val driverResult = authStore.register(
            name = "Fake Driver",
            email = "fake@driver.com",
            password = "securePassword123",
            role = AppMode.DELIVERY_PARTNER
        )
        assertTrue("Delivery driver self-registration must be blocked", driverResult is AuthResult.Failure)
    }

    @Test
    fun `test invalid credentials rejection`() {
        // Short password (< 8 chars)
        val shortPassResult = authStore.register(
            name = "John",
            email = "john@example.com",
            password = "123",
            role = AppMode.CUSTOMER
        )
        assertTrue("Short password must fail", shortPassResult is AuthResult.Failure)

        // Invalid email (no @)
        val invalidEmailResult = authStore.register(
            name = "John",
            email = "invalidemailformat",
            password = "securePassword123",
            role = AppMode.CUSTOMER
        )
        assertTrue("Invalid email format must fail", invalidEmailResult is AuthResult.Failure)

        // Blank name
        val blankNameResult = authStore.register(
            name = "   ",
            email = "john@example.com",
            password = "securePassword123",
            role = AppMode.CUSTOMER
        )
        assertTrue("Blank name must fail", blankNameResult is AuthResult.Failure)
    }

    @Test
    fun `test sign in and session persistence`() {
        // Register customer
        val regResult = authStore.register(
            name = "Maya Lin",
            email = "maya@example.com",
            password = "password123",
            role = AppMode.CUSTOMER
        )
        assertTrue(regResult is AuthResult.Success)

        // Sign in
        val signInResult = authStore.signIn(
            email = "maya@example.com",
            password = "password123"
        )
        assertTrue("Sign in should succeed", signInResult is AuthResult.Success)
        val signedInUser = (signInResult as AuthResult.Success).user
        assertEquals("maya@example.com", signedInUser.email)

        // Current user session check
        val sessionUser = authStore.currentUser()
        assertNotNull("Session should persist current user", sessionUser)
        assertEquals("maya@example.com", sessionUser?.email)
    }

    @Test
    fun `test sign out clears active session`() {
        authStore.register(
            name = "Test User",
            email = "test@example.com",
            password = "password123",
            role = AppMode.CUSTOMER
        )
        assertNotNull(authStore.currentUser())

        authStore.signOut()
        assertNull("Session must be null after sign out", authStore.currentUser())
    }

    @Test
    fun `test role routing and work linking for provisioned accounts`() {
        // 1. Admin account routing
        val adminResult = authStore.signIn("admin@eatfine.com", "password123")
        assertTrue(adminResult is AuthResult.Success)
        val adminUser = (adminResult as AuthResult.Success).user
        assertEquals(AppMode.ADMIN, adminUser.role)
        assertEquals("Platform Administrator", adminUser.name)

        // 2. Business account routing with linked restaurant
        val partnerResult = authStore.signIn("partner@eatfine.com", "password123")
        assertTrue(partnerResult is AuthResult.Success)
        val partnerUser = (partnerResult as AuthResult.Success).user
        assertEquals(AppMode.RESTAURANT_PARTNER, partnerUser.role)
        assertEquals("rest_1", partnerUser.restaurantId)

        // 3. Delivery agent routing with linked driver profile
        val driverResult = authStore.signIn("driver@eatfine.com", "password123")
        assertTrue(driverResult is AuthResult.Success)
        val driverUser = (driverResult as AuthResult.Success).user
        assertEquals(AppMode.DELIVERY_PARTNER, driverUser.role)
        assertEquals("driver_1", driverUser.driverId)
    }

    @Test
    fun `test password reset validation`() {
        val validReset = authStore.sendPasswordReset("customer@eatfine.com")
        assertTrue("Valid email reset should succeed", validReset.isSuccess)

        val invalidReset = authStore.sendPasswordReset("notanemail")
        assertTrue("Invalid email reset should fail", invalidReset.isFailure)
    }

    @Test
    fun `test guest sign in creates guest diner profile`() {
        val guest = authStore.guestSignIn()
        assertEquals("Guest Diner", guest.name)
        assertEquals(AppMode.CUSTOMER, guest.role)
        assertEquals("guest@eatfine.com", guest.email)
        assertEquals("guest_user", guest.uid)
    }
}
