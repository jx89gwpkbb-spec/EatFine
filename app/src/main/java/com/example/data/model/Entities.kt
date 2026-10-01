package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppMode {
    CUSTOMER,
    RESTAURANT_PARTNER,
    DELIVERY_PARTNER,
    ADMIN
}

enum class OrderStatus(val display: String, val stepIndex: Int) {
    PLACED("Order Placed", 0),
    ACCEPTED("Restaurant Accepted", 1),
    PREPARING("Kitchen Preparing", 2),
    READY_FOR_PICKUP("Ready for Driver", 3),
    ON_THE_WAY("Out for Delivery", 4),
    DELIVERED("Delivered", 5),
    CANCELLED("Cancelled", -1)
}

enum class DeliveryType(val label: String) {
    DELIVERY("Doorstep Delivery"),
    PICKUP("Self Pickup")
}

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val tagline: String,
    val cuisines: String, // comma separated e.g. "Wood-Fired Pizza, Artisan Pasta, Salads"
    val rating: Float,
    val reviewCount: Int,
    val deliveryTimeMin: Int,
    val deliveryFee: Double,
    val minOrder: Double,
    val priceTier: Int, // 1 to 4 ($ to $$$$)
    val distanceKm: Double,
    val address: String,
    val isPureVeg: Boolean,
    val dietaryRestrictionsCsv: String, // comma separated DietaryRestriction names e.g. "VEGETARIAN,VEGAN,GLUTEN_FREE"
    val isPromoted: Boolean,
    val isOpen: Boolean,
    val offerText: String,
    val bannerDrawableRes: Int,
    val heroCategory: String, // "Pizza", "Healthy", "Burgers", "Biryani", "Bakery"
    val certificationNote: String = "Kitchen adheres to dietary cross-contamination protocols.",
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194
) {
    val dietaryList: List<DietaryRestriction>
        get() = dietaryRestrictionsCsv.split(",")
            .mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() } }
            .mapNotNull { DietaryRestriction.fromKey(it) }
}

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String, // e.g. "Starters", "Mains", "Bowls", "Breads", "Desserts", "Beverages"
    val isVeg: Boolean,
    val dietaryRestrictionsCsv: String, // comma separated
    val calories: Int,
    val spiceLevel: Int, // 0 = Mild, 1 = Medium, 2 = Hot, 3 = Extra Hot
    val isAvailable: Boolean = true,
    val isBestseller: Boolean = false,
    val rating: Float = 4.8f
) {
    val dietaryList: List<DietaryRestriction>
        get() = dietaryRestrictionsCsv.split(",")
            .mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() } }
            .mapNotNull { DietaryRestriction.fromKey(it) }
}

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,
    val restaurantId: String,
    val menuItemId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val isVeg: Boolean,
    val dietaryRestrictionsCsv: String,
    val specialDietaryNote: String = "",
    val userId: String = ""
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val restaurantId: String,
    val restaurantName: String,
    val totalAmount: Double,
    val subtotal: Double,
    val deliveryFee: Double,
    val taxAndFees: Double,
    val discount: Double,
    val status: OrderStatus,
    val deliveryAddress: String,
    val deliveryType: DeliveryType,
    val placedTimestamp: Long,
    val estimatedDeliveryMinutes: Int,
    val driverName: String = "Alex Rivera",
    val driverPhone: String = "+1 (555) 438-9201",
    val driverVehicle: String = "Honda Eco Scooter • #EF-789",
    val itemsSummary: String, // JSON or formatted text of items
    val chefDietaryInstructions: String = "",
    val paymentMethod: String = "EatFine Pay (Card)",
    val userId: String = ""
)

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val date: String,
    val time: String,
    val guests: Int,
    val dietaryNotes: String, // e.g. "2 Vegan, 1 Gluten-Free guest"
    val status: String = "Confirmed",
    val bookedTimestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val restaurantId: String,
    val userName: String,
    val rating: Float,
    val comment: String,
    val dietaryTagsUsed: String,
    val date: String
)

@Entity(tableName = "favorites", primaryKeys = ["userId", "restaurantId"])
data class FavoriteEntity(
    val userId: String,
    val restaurantId: String,
    val savedTimestamp: Long = System.currentTimeMillis()
)

data class DietaryFilterState(
    val selectedRestrictions: Set<DietaryRestriction> = emptySet(),
    val matchAllSelected: Boolean = false, // false = match ANY, true = match ALL
    val pureVegOnly: Boolean = false,
    val sortBy: SortOption = SortOption.RECOMMENDED,
    val minRating: Float = 0f,
    val maxDeliveryTimeMinutes: Int = 60,
    val maxPriceTier: Int = 4,
    val query: String = ""
) {
    val isActive: Boolean
        get() = selectedRestrictions.isNotEmpty() || pureVegOnly || minRating > 0f || maxDeliveryTimeMinutes < 60 || maxPriceTier < 4 || sortBy != SortOption.RECOMMENDED
}

enum class SortOption(val label: String) {
    RECOMMENDED("Recommended"),
    RATING("Highest Rated"),
    DELIVERY_TIME("Fastest Delivery"),
    PRICE_LOW("Price: Low to High"),
    DISTANCE("Nearest")
}
