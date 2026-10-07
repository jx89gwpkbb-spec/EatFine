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

    @Test
    fun `test admin create user provisions new partners and drivers`() {
        // Admin creates a restaurant partner
        val partnerRes = authStore.adminCreateUser(
            name = "Chef Luigi",
            email = "luigi@pastaparadiso.com",
            password = "securePassword123",
            role = AppMode.RESTAURANT_PARTNER,
            restaurantId = "rest_2"
        )
        assertTrue("Admin creating partner must succeed", partnerRes is AuthResult.Success)
        val partner = (partnerRes as AuthResult.Success).user
        assertEquals("Chef Luigi", partner.name)
        assertEquals(AppMode.RESTAURANT_PARTNER, partner.role)
        assertEquals("rest_2", partner.restaurantId)

        // Admin creates a delivery driver
        val driverRes = authStore.adminCreateUser(
            name = "Racer Rick",
            email = "rick@eatfine.com",
            password = "securePassword123",
            role = AppMode.DELIVERY_PARTNER,
            driverId = "driver_99"
        )
        assertTrue("Admin creating driver must succeed", driverRes is AuthResult.Success)
        val driver = (driverRes as AuthResult.Success).user
        assertEquals("Racer Rick", driver.name)
        assertEquals(AppMode.DELIVERY_PARTNER, driver.role)
        assertEquals("driver_99", driver.driverId)

        // Check user directory contains new users
        val allUsers = authStore.getAllUsers()
        assertTrue("Directory should contain new partner", allUsers.any { it.email == "luigi@pastaparadiso.com" })
        assertTrue("Directory should contain new driver", allUsers.any { it.email == "rick@eatfine.com" })
    }

    @Test
    fun `test admin cannot create duplicate email user`() {
        val first = authStore.adminCreateUser(
            name = "Original User",
            email = "unique@example.com",
            password = "password123",
            role = AppMode.CUSTOMER
        )
        assertTrue(first is AuthResult.Success)

        val duplicate = authStore.adminCreateUser(
            name = "Duplicate User",
            email = "unique@example.com",
            password = "password456",
            role = AppMode.CUSTOMER
        )
        assertTrue("Duplicate creation should fail", duplicate is AuthResult.Failure)
    }

    @Test
    fun `test settings models updates`() {
        val userSettings = com.example.data.model.UserProfileSettings(
            displayName = "Alex",
            phone = "+1 555 1234",
            defaultAddress = "123 Main St",
            contactlessDelivery = true
        )
        assertEquals("Alex", userSettings.displayName)

        val adminSettings = com.example.data.model.AdminPlatformSettings(
            platformCommissionPercent = 18.0,
            baseDeliveryFee = 3.50,
            isPlatformOpen = true
        )
        assertEquals(18.0, adminSettings.platformCommissionPercent, 0.01)

        val businessSettings = com.example.data.model.BusinessOwnerSettings(
            restaurantName = "The Rustic Fork",
            isKitchenBusySurge = true,
            defaultPrepMinutes = 35
        )
        assertTrue(businessSettings.isKitchenBusySurge)
        assertEquals(35, businessSettings.defaultPrepMinutes)

        val deliverySettings = com.example.data.model.DeliveryAgentSettings(
            driverName = "David",
            vehicleType = "Electric Bicycle",
            isOnline = true
        )
        assertEquals("Electric Bicycle", deliverySettings.vehicleType)
    }

    @Test
    fun `test UserProfile data model fields and Firestore serialization`() {
        // Default constructor
        val defaultProfile = com.example.data.model.UserProfile()
        assertEquals("", defaultProfile.userId)
        assertEquals("", defaultProfile.email)
        assertEquals("customer", defaultProfile.role)

        // 3-field constructor with userId, email, and role
        val customerProfile = com.example.data.model.UserProfile(
            userId = "usr_cust_01",
            email = "customer@eatfine.com",
            role = "customer"
        )
        assertEquals("usr_cust_01", customerProfile.userId)
        assertEquals("customer@eatfine.com", customerProfile.email)
        assertEquals("customer", customerProfile.role)
        assertEquals(com.example.data.model.UserRole.CUSTOMER, customerProfile.userRole)
        assertEquals(AppMode.CUSTOMER, customerProfile.toAppMode())

        // Business owner profile
        val businessProfile = com.example.data.model.UserProfile(
            userId = "usr_biz_01",
            email = "partner@eatfine.com",
            role = "business_owner"
        )
        assertEquals("business_owner", businessProfile.role)
        assertEquals(com.example.data.model.UserRole.BUSINESS_OWNER, businessProfile.userRole)
        assertEquals(AppMode.RESTAURANT_PARTNER, businessProfile.toAppMode())

        // Delivery agent profile
        val deliveryProfile = com.example.data.model.UserProfile(
            userId = "usr_drv_01",
            email = "driver@eatfine.com",
            role = "delivery_agent"
        )
        assertEquals("delivery_agent", deliveryProfile.role)
        assertEquals(com.example.data.model.UserRole.DELIVERY_AGENT, deliveryProfile.userRole)
        assertEquals(AppMode.DELIVERY_PARTNER, deliveryProfile.toAppMode())

        // Admin profile
        val adminProfile = com.example.data.model.UserProfile(
            userId = "usr_adm_01",
            email = "admin@eatfine.com",
            role = "admin"
        )
        assertEquals("admin", adminProfile.role)
        assertEquals(com.example.data.model.UserRole.ADMIN, adminProfile.userRole)
        assertEquals(AppMode.ADMIN, adminProfile.toAppMode())

        // toMap export for Firestore
        val map = customerProfile.toMap()
        assertEquals("usr_cust_01", map["userId"])
        assertEquals("customer@eatfine.com", map["email"])
        assertEquals("customer", map["role"])
    }

    @Test
    fun `test UserProfileRepository stores and retrieves profiles`() = kotlinx.coroutines.runBlocking {
        val repo = com.example.data.repository.UserProfileRepository("ai-studio-android-eatfine-843821e6-404f-47a6-b22a-2d675a5a4d40", context)

        val profile = com.example.data.model.UserProfile(
            userId = "test_user_42",
            email = "tester@eatfine.com",
            role = "customer"
        )
        val saveResult = repo.saveUserProfile(profile)
        assertTrue("Saving user profile must succeed", saveResult.isSuccess)

        val retrieved = repo.getUserProfile("test_user_42").getOrNull()
        assertNotNull("Retrieved profile should not be null", retrieved)
        assertEquals("test_user_42", retrieved?.userId)
        assertEquals("tester@eatfine.com", retrieved?.email)
        assertEquals("customer", retrieved?.role)

        // Update role to business_owner
        val updateResult = repo.updateUserRole("test_user_42", "business_owner")
        assertTrue("Updating role must succeed", updateResult.isSuccess)
        val updated = repo.getUserProfile("test_user_42").getOrNull()
        assertEquals("business_owner", updated?.role)

        // Delete profile
        val deleteResult = repo.deleteUserProfile("test_user_42")
        assertTrue("Deleting user profile must succeed", deleteResult.isSuccess)
        val afterDelete = repo.getUserProfile("test_user_42").getOrNull()
        assertNull("Profile should be null after delete", afterDelete)
    }
}
