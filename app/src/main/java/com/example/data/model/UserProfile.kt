package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * Supported user roles in the EatFine platform for Firestore access control.
 * Matches: admin, business_owner, customer, delivery_agent.
 */
enum class UserRole(val value: String) {
    ADMIN("admin"),
    BUSINESS_OWNER("business_owner"),
    CUSTOMER("customer"),
    DELIVERY_AGENT("delivery_agent");

    fun toAppMode(): AppMode {
        return when (this) {
            ADMIN -> AppMode.ADMIN
            BUSINESS_OWNER -> AppMode.RESTAURANT_PARTNER
            DELIVERY_AGENT -> AppMode.DELIVERY_PARTNER
            CUSTOMER -> AppMode.CUSTOMER
        }
    }

    companion object {
        fun fromValue(value: String?): UserRole {
            return when (value?.lowercase()?.trim()) {
                "admin" -> ADMIN
                "business_owner", "business", "restaurant_partner" -> BUSINESS_OWNER
                "delivery_agent", "delivery", "delivery_partner" -> DELIVERY_AGENT
                else -> CUSTOMER
            }
        }
    }
}

/**
 * Data model class for UserProfile representing a user document in Firestore (`/users/{userId}`).
 * Includes fields for userId, email, and role (admin, business_owner, customer, delivery_agent).
 *
 * Implements no-argument default constructor required for Firestore document deserialization
 * via document.toObject(UserProfile::class.java).
 */
@IgnoreExtraProperties
data class UserProfile(
    @get:PropertyName("userId")
    @set:PropertyName("userId")
    var userId: String = "",

    @get:PropertyName("email")
    @set:PropertyName("email")
    var email: String = "",

    @get:PropertyName("role")
    @set:PropertyName("role")
    var role: String = UserRole.CUSTOMER.value,

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("restaurantId")
    @set:PropertyName("restaurantId")
    var restaurantId: String? = null,

    @get:PropertyName("driverId")
    @set:PropertyName("driverId")
    var driverId: String? = null,

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis()
) {
    // Secondary constructor matching exact required fields: userId, email, role
    constructor(userId: String, email: String, role: String) : this(
        userId = userId,
        email = email,
        role = role,
        name = "",
        restaurantId = null,
        driverId = null,
        createdAt = System.currentTimeMillis()
    )

    // Secondary constructor with typed UserRole
    constructor(userId: String, email: String, userRole: UserRole) : this(
        userId = userId,
        email = email,
        role = userRole.value
    )

    val userRole: UserRole
        get() = UserRole.fromValue(role)

    fun toAppMode(): AppMode {
        return when (userRole) {
            UserRole.ADMIN -> AppMode.ADMIN
            UserRole.BUSINESS_OWNER -> AppMode.RESTAURANT_PARTNER
            UserRole.DELIVERY_AGENT -> AppMode.DELIVERY_PARTNER
            UserRole.CUSTOMER -> AppMode.CUSTOMER
        }
    }

    fun toMap(): Map<String, Any?> {
        return buildMap {
            put("userId", userId)
            put("email", email)
            put("role", role)
            put("name", name)
            if (restaurantId != null) put("restaurantId", restaurantId)
            if (driverId != null) put("driverId", driverId)
            put("createdAt", createdAt)
        }
    }

    companion object {
        fun fromAppMode(
            userId: String,
            email: String,
            name: String,
            mode: AppMode,
            restaurantId: String? = null,
            driverId: String? = null
        ): UserProfile {
            val roleStr = when (mode) {
                AppMode.ADMIN -> UserRole.ADMIN.value
                AppMode.RESTAURANT_PARTNER -> UserRole.BUSINESS_OWNER.value
                AppMode.DELIVERY_PARTNER -> UserRole.DELIVERY_AGENT.value
                AppMode.CUSTOMER -> UserRole.CUSTOMER.value
            }
            return UserProfile(
                userId = userId,
                email = email,
                role = roleStr,
                name = name,
                restaurantId = restaurantId,
                driverId = driverId
            )
        }
    }
}
