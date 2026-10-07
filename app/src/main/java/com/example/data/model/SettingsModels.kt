package com.example.data.model

data class UserProfileSettings(
    val displayName: String = "Alex Rivera",
    val email: String = "customer@eatfine.com",
    val phone: String = "+1 (555) 234-5678",
    val defaultAddress: String = "452 Market Street, Apt 4B, San Francisco, CA",
    val selectedDietaryRestrictions: Set<DietaryRestriction> = setOf(DietaryRestriction.GLUTEN_FREE, DietaryRestriction.VEGAN),
    val pushNotificationsEnabled: Boolean = true,
    val smsUpdatesEnabled: Boolean = true,
    val contactlessDelivery: Boolean = true
)

data class AdminPlatformSettings(
    val platformCommissionPercent: Double = 15.0,
    val baseDeliveryFee: Double = 2.99,
    val surgeDeliveryMultiplier: Double = 1.0,
    val isPlatformOpen: Boolean = true,
    val systemAnnouncement: String = "Welcome to EatFine! 100% dietary-safe kitchens verified daily.",
    val requireDietaryAuditForListing: Boolean = true,
    val autoAssignDrivers: Boolean = true
)

data class BusinessOwnerSettings(
    val restaurantId: String = "rest_1",
    val restaurantName: String = "The Rustic Fork",
    val cuisines: String = "Wood-Fired Pizza, Artisan Pasta, Salads",
    val phoneNumber: String = "+1 (415) 890-1234",
    val address: String = "842 Valencia St, Mission District, SF",
    val operatingHours: String = "11:00 AM - 10:00 PM",
    val isOpen: Boolean = true,
    val isKitchenBusySurge: Boolean = false,
    val autoAcceptOrders: Boolean = true,
    val defaultPrepMinutes: Int = 25,
    val minimumOrderValue: Double = 15.00,
    val deliveryRadiusMiles: Double = 6.0,
    val hasDedicatedFryer: Boolean = true,
    val allergenCrossContaminationCertified: Boolean = true,
    val bankAccountMasked: String = "Chase Business •••• 4819",
    val payoutSchedule: String = "Daily Automatic"
)

data class DeliveryAgentSettings(
    val driverId: String = "driver_1",
    val driverName: String = "David Driver",
    val driverPhone: String = "+1 (555) 438-9201",
    val vehicleType: String = "Honda Eco Scooter",
    val vehiclePlate: String = "EF-7890",
    val isOnline: Boolean = true,
    val autoAcceptDeliveries: Boolean = false,
    val maxDeliveryRadiusMiles: Double = 8.0,
    val thermalBagVerified: Boolean = true,
    val navigationApp: String = "Google Maps",
    val dailyEarningsTarget: Double = 120.00,
    val payoutAccountMasked: String = "Wells Fargo •••• 9012"
)
