package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.CartItemEntity
import com.example.data.model.DeliveryType
import com.example.data.model.DietaryFilterState
import com.example.data.model.FavoriteEntity
import com.example.data.model.MenuItemEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.ReservationEntity
import com.example.data.model.RestaurantEntity
import com.example.data.model.ReviewEntity
import com.example.data.model.SortOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EatFineRepository(
    private val database: AppDatabase,
    private val userId: StateFlow<String?>,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val restaurantDao = database.restaurantDao()
    private val menuItemDao = database.menuItemDao()
    private val cartDao = database.cartDao()
    private val orderDao = database.orderDao()
    private val reservationDao = database.reservationDao()
    private val favoriteDao = database.favoriteDao()
    private val reviewDao = database.reviewDao()

    init {
        externalScope.launch {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        if (restaurantDao.getCount() == 0) {
            restaurantDao.insertRestaurants(SeedData.sampleRestaurants)
            menuItemDao.insertMenuItems(SeedData.sampleMenuItems)
            for (rev in SeedData.sampleReviews) {
                reviewDao.insertReview(rev)
            }
        }
    }

    // Restaurants Flow
    fun getAllRestaurants(): Flow<List<RestaurantEntity>> = restaurantDao.getAllRestaurants()

    fun getRestaurantById(id: String): Flow<RestaurantEntity?> = restaurantDao.getRestaurantById(id)

    fun getMenuItemsForRestaurant(restaurantId: String): Flow<List<MenuItemEntity>> =
        menuItemDao.getMenuItemsByRestaurant(restaurantId)

    fun getAllMenuItems(): Flow<List<MenuItemEntity>> = menuItemDao.getAllMenuItems()

    // Filtered Restaurants Flow based on Dietary Restrictions and Filter State
    fun getFilteredRestaurants(filterState: Flow<DietaryFilterState>): Flow<List<RestaurantEntity>> {
        return combine(getAllRestaurants(), filterState) { list, filter ->
            list.filter { restaurant ->
                // Search query match
                val matchesQuery = filter.query.isBlank() ||
                        restaurant.name.contains(filter.query, ignoreCase = true) ||
                        restaurant.tagline.contains(filter.query, ignoreCase = true) ||
                        restaurant.cuisines.contains(filter.query, ignoreCase = true) ||
                        restaurant.heroCategory.contains(filter.query, ignoreCase = true)

                // Pure Veg filter
                val matchesPureVeg = !filter.pureVegOnly || restaurant.isPureVeg

                // Dietary Restrictions filter
                val matchesDietary = if (filter.selectedRestrictions.isEmpty()) {
                    true
                } else {
                    val restDietary = restaurant.dietaryList.toSet()
                    if (filter.matchAllSelected) {
                        restDietary.containsAll(filter.selectedRestrictions)
                    } else {
                        filter.selectedRestrictions.any { it in restDietary }
                    }
                }

                // Rating filter
                val matchesRating = restaurant.rating >= filter.minRating

                // Delivery time filter
                val matchesDeliveryTime = restaurant.deliveryTimeMin <= filter.maxDeliveryTimeMinutes

                // Price tier filter
                val matchesPrice = restaurant.priceTier <= filter.maxPriceTier

                matchesQuery && matchesPureVeg && matchesDietary && matchesRating && matchesDeliveryTime && matchesPrice
            }.let { filtered ->
                // Sort
                when (filter.sortBy) {
                    SortOption.RECOMMENDED -> filtered.sortedByDescending { it.isPromoted }
                    SortOption.RATING -> filtered.sortedByDescending { it.rating }
                    SortOption.DELIVERY_TIME -> filtered.sortedBy { it.deliveryTimeMin }
                    SortOption.PRICE_LOW -> filtered.sortedBy { it.priceTier }
                    SortOption.DISTANCE -> filtered.sortedBy { it.distanceKm }
                }
            }
        }
    }

    // Cart Operations
    fun getCartItems(): Flow<List<CartItemEntity>> = userId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else cartDao.getCartItems(uid)
    }

    suspend fun addToCart(
        menuItem: MenuItemEntity,
        specialDietaryNote: String = ""
    ): Boolean {
        val uid = userId.value ?: return false
        val currentItems = cartDao.getCartItems(uid).first()
        // If cart has items from another restaurant, reset cart to new restaurant
        if (currentItems.isNotEmpty() && currentItems.first().restaurantId != menuItem.restaurantId) {
            cartDao.clearCart(uid)
        }

        val existing = cartDao.getItemByMenuId(menuItem.id, uid)
        if (existing != null) {
            cartDao.updateCartItemQuantity(existing.id, uid, existing.quantity + 1)
        } else {
            cartDao.insertCartItem(
                CartItemEntity(
                    id = UUID.randomUUID().toString(),
                    restaurantId = menuItem.restaurantId,
                    menuItemId = menuItem.id,
                    name = menuItem.name,
                    price = menuItem.price,
                    quantity = 1,
                    isVeg = menuItem.isVeg,
                    dietaryRestrictionsCsv = menuItem.dietaryRestrictionsCsv,
                    specialDietaryNote = specialDietaryNote,
                    userId = uid
                )
            )
        }
        return true
    }

    suspend fun updateCartQuantity(cartItemId: String, newQuantity: Int) {
        val uid = userId.value ?: return
        if (newQuantity <= 0) {
            cartDao.deleteCartItemById(cartItemId, uid)
        } else {
            val items = cartDao.getCartItems(uid).first()
            val item = items.find { it.id == cartItemId } ?: return
            cartDao.updateCartItemQuantity(item.id, uid, newQuantity)
        }
    }

    suspend fun clearCart() {
        userId.value?.let { cartDao.clearCart(it) }
    }

    // Orders
    fun getAllOrders(): Flow<List<OrderEntity>> = userId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else orderDao.getAllOrders(uid)
    }

    fun getOrderById(orderId: String): Flow<OrderEntity?> = userId.flatMapLatest { uid ->
        if (uid == null) flowOf(null) else orderDao.getOrderById(orderId, uid)
    }

    suspend fun placeOrder(
        restaurant: RestaurantEntity,
        cartItems: List<CartItemEntity>,
        deliveryAddress: String,
        deliveryType: DeliveryType,
        appliedCoupon: String?,
        chefDietaryNotes: String
    ): OrderEntity {
        val uid = userId.value ?: throw IllegalStateException("Sign in before placing an order.")
        val subtotal = cartItems.sumOf { it.price * it.quantity }
        val discount = when (appliedCoupon?.uppercase()) {
            "EATFINE50" -> (subtotal * 0.50).coerceAtMost(10.0)
            "HEALTHY20" -> (subtotal * 0.20).coerceAtMost(8.0)
            "FREESHIP" -> restaurant.deliveryFee
            else -> 0.0
        }
        val actualDeliveryFee = if (deliveryType == DeliveryType.PICKUP || appliedCoupon == "FREESHIP") 0.0 else restaurant.deliveryFee
        val taxAndFees = (subtotal * 0.08) + 1.25 // tax + packaging
        val total = (subtotal - discount + actualDeliveryFee + taxAndFees).coerceAtLeast(0.0)

        val itemsSummary = cartItems.joinToString(", ") { "${it.quantity}x ${it.name}" }
        val order = OrderEntity(
            orderId = "EF-" + (100000..999999).random(),
            restaurantId = restaurant.id,
            restaurantName = restaurant.name,
            totalAmount = total,
            subtotal = subtotal,
            deliveryFee = actualDeliveryFee,
            taxAndFees = taxAndFees,
            discount = discount,
            status = OrderStatus.PLACED,
            deliveryAddress = deliveryAddress,
            deliveryType = deliveryType,
            placedTimestamp = System.currentTimeMillis(),
            estimatedDeliveryMinutes = restaurant.deliveryTimeMin,
            itemsSummary = itemsSummary,
            chefDietaryInstructions = chefDietaryNotes,
            userId = uid
        )

        orderDao.insertOrder(order)
        cartDao.clearCart(uid)
        return order
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        userId.value?.let { orderDao.updateOrderStatus(orderId, it, status) }
    }

    suspend fun reorderToCart(order: OrderEntity): Int {
        val uid = userId.value ?: return 0
        cartDao.clearCart(uid)
        val menuItems = menuItemDao.getMenuItemsByRestaurant(order.restaurantId).first()
        val itemsParts = order.itemsSummary.split(",")
        var addedCount = 0

        for (part in itemsParts) {
            val trimmed = part.trim()
            if (trimmed.isEmpty()) continue
            val qty = trimmed.substringBefore("x ").trim().toIntOrNull() ?: 1
            val itemName = trimmed.substringAfter("x ").trim()

            // Find matching menu item in the restaurant's menu
            val matchedItem = menuItems.find { 
                it.name.equals(itemName, ignoreCase = true) || 
                it.name.contains(itemName, ignoreCase = true) || 
                itemName.contains(it.name, ignoreCase = true)
            } ?: menuItems.firstOrNull()

            if (matchedItem != null) {
                cartDao.insertCartItem(
                    CartItemEntity(
                        id = UUID.randomUUID().toString(),
                        restaurantId = matchedItem.restaurantId,
                        menuItemId = matchedItem.id,
                        name = matchedItem.name,
                        price = matchedItem.price,
                        quantity = qty,
                        isVeg = matchedItem.isVeg,
                        dietaryRestrictionsCsv = matchedItem.dietaryRestrictionsCsv,
                        specialDietaryNote = order.chefDietaryInstructions,
                        userId = uid
                    )
                )
                addedCount += qty
            }
        }
        return addedCount
    }

    suspend fun instantReorder(order: OrderEntity): OrderEntity {
        val uid = userId.value ?: throw IllegalStateException("Sign in before reordering.")
        val newOrderId = "EF-" + (100000..999999).random()
        val newOrder = order.copy(
            orderId = newOrderId,
            status = OrderStatus.PLACED,
            placedTimestamp = System.currentTimeMillis(),
            userId = uid
        )
        orderDao.insertOrder(newOrder)
        return newOrder
    }

    // Reservations
    fun getAllReservations(): Flow<List<ReservationEntity>> = userId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else reservationDao.getAllReservations(uid)
    }

    suspend fun bookReservation(
        restaurant: RestaurantEntity,
        date: String,
        time: String,
        guests: Int,
        dietaryNotes: String
    ): ReservationEntity {
        val uid = userId.value ?: throw IllegalStateException("Sign in before booking a reservation.")
        val reservation = ReservationEntity(
            id = UUID.randomUUID().toString(),
            restaurantId = restaurant.id,
            restaurantName = restaurant.name,
            date = date,
            time = time,
            guests = guests,
            dietaryNotes = dietaryNotes,
            status = "Confirmed",
            userId = uid
        )
        reservationDao.insertReservation(reservation)
        return reservation
    }

    // Favorites
    fun getFavoriteIds(): Flow<List<String>> = userId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else favoriteDao.getFavoriteIds(uid)
    }

    suspend fun toggleFavorite(restaurantId: String) {
        val uid = userId.value ?: return
        val isFav = favoriteDao.isFavorite(restaurantId, uid)
        if (isFav) {
            favoriteDao.removeFavorite(restaurantId, uid)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(userId = uid, restaurantId = restaurantId))
        }
    }

    // Reviews
    fun getReviewsForRestaurant(restaurantId: String): Flow<List<ReviewEntity>> =
        reviewDao.getReviewsForRestaurant(restaurantId)

    suspend fun addReview(review: ReviewEntity) = reviewDao.insertReview(review)

    suspend fun submitReview(
        restaurantId: String,
        userName: String,
        rating: Float,
        comment: String,
        dietaryTagsUsed: String
    ): ReviewEntity {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val review = ReviewEntity(
            id = "rev_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
            restaurantId = restaurantId,
            userName = userName.ifBlank { "Verified Diner" },
            rating = rating,
            comment = comment.trim(),
            dietaryTagsUsed = dietaryTagsUsed,
            date = dateFormat.format(Date())
        )
        reviewDao.insertReview(review)

        // Recompute average rating and total count for the restaurant
        val count = reviewDao.getReviewCount(restaurantId)
        val avg = reviewDao.getAverageRating(restaurantId) ?: rating
        val roundedAvg = (Math.round(avg * 10.0) / 10.0).toFloat()
        restaurantDao.updateRestaurantRating(restaurantId, roundedAvg, count)

        return review
    }

    // Partner Menu Controls
    suspend fun toggleMenuItemAvailability(menuItemId: String, isAvailable: Boolean) {
        menuItemDao.setAvailability(menuItemId, isAvailable)
    }

    suspend fun updateRestaurantOpenStatus(restaurantId: String, isOpen: Boolean) {
        val rest = restaurantDao.getRestaurantByIdDirect(restaurantId) ?: return
        restaurantDao.updateRestaurant(rest.copy(isOpen = isOpen))
    }
}
