package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthResult
import com.example.data.auth.AuthStore
import com.example.data.auth.AuthUser
import com.example.data.local.AppDatabase
import com.example.data.model.AppMode
import com.example.data.model.CartItemEntity
import com.example.data.model.DeliveryType
import com.example.data.model.DietaryFilterState
import com.example.data.model.DietaryRestriction
import com.example.data.model.MenuItemEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.ReservationEntity
import com.example.data.model.RestaurantEntity
import com.example.data.model.ReviewEntity
import com.example.data.model.SortOption
import com.example.data.repository.EatFineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class CustomerTab(val label: String) {
    HOME("Home"),
    EXPLORE("Explore"),
    ORDERS("Orders"),
    FAVORITES("Saved"),
    RESERVATIONS("Tables"),
    PROFILE("Profile")
}

class EatFineViewModel(application: Application) : AndroidViewModel(application) {

    private val authStore = AuthStore(application)
    private val _authUser = MutableStateFlow<AuthUser?>(null)
    val authUser: StateFlow<AuthUser?> = _authUser.asStateFlow()
    private val _authInitialized = MutableStateFlow(false)
    val authInitialized: StateFlow<Boolean> = _authInitialized.asStateFlow()
    private val _activeUserId = MutableStateFlow<String?>(null)

    private val repository = EatFineRepository(
        database = AppDatabase.getInstance(application),
        userId = _activeUserId,
        externalScope = viewModelScope
    )

    // Role & Navigation State
    private val _appMode = MutableStateFlow(AppMode.CUSTOMER)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val user = withContext(Dispatchers.IO) { authStore.currentUser() }
                _authUser.value = user
                _activeUserId.value = user?.uid
                _appMode.value = user?.role ?: AppMode.CUSTOMER
            } catch (_: Exception) {
                _authUser.value = null
                _activeUserId.value = null
                _appMode.value = AppMode.CUSTOMER
            } finally {
                _authInitialized.value = true
            }
        }
    }

    private val _customerTab = MutableStateFlow(CustomerTab.HOME)
    val customerTab: StateFlow<CustomerTab> = _customerTab.asStateFlow()

    private val _selectedRestaurantId = MutableStateFlow<String?>(null)
    val selectedRestaurantId: StateFlow<String?> = _selectedRestaurantId.asStateFlow()

    private val _trackingOrderId = MutableStateFlow<String?>(null)
    val trackingOrderId: StateFlow<String?> = _trackingOrderId.asStateFlow()

    private val _isFilterSheetVisible = MutableStateFlow(false)
    val isFilterSheetVisible: StateFlow<Boolean> = _isFilterSheetVisible.asStateFlow()

    // Filter & Search State
    private val _filterState = MutableStateFlow(DietaryFilterState())
    val filterState: StateFlow<DietaryFilterState> = _filterState.asStateFlow()

    // User Profile / Settings
    val deliveryAddress = MutableStateFlow("742 Evergreen Terrace, Apt 4B")
    val deliveryType = MutableStateFlow(DeliveryType.DELIVERY)
    val chefDietaryNotes = MutableStateFlow("")
    val activeCoupon = MutableStateFlow<String?>("EATFINE50")
    val rewardPoints = MutableStateFlow(480)

    // Repository Flows
    val allRestaurants: StateFlow<List<RestaurantEntity>> = repository.getAllRestaurants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRestaurants: StateFlow<List<RestaurantEntity>> =
        repository.getFilteredRestaurants(_filterState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItemEntity>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeOngoingOrder: StateFlow<OrderEntity?> = allOrders.map { orders ->
        orders.firstOrNull { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val favoriteIds: StateFlow<Set<String>> = repository.getFavoriteIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val allReservations: StateFlow<List<ReservationEntity>> = repository.getAllReservations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart summary computations
    val cartSubtotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartItemCount: StateFlow<Int> = cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Current Selected Restaurant and its Menu Items
    val currentRestaurant: StateFlow<RestaurantEntity?> = combine(allRestaurants, selectedRestaurantId) { list, id ->
        list.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun getMenuItemsForRestaurant(restaurantId: String) =
        repository.getMenuItemsForRestaurant(restaurantId)

    fun getReviewsForRestaurant(restaurantId: String) =
        repository.getReviewsForRestaurant(restaurantId)

    // Dietary Filter Handlers
    fun toggleDietaryRestriction(restriction: DietaryRestriction) {
        val current = _filterState.value.selectedRestrictions
        val updated = if (restriction in current) {
            current - restriction
        } else {
            current + restriction
        }
        _filterState.value = _filterState.value.copy(selectedRestrictions = updated)
    }

    fun setMatchAllDietary(matchAll: Boolean) {
        _filterState.value = _filterState.value.copy(matchAllSelected = matchAll)
    }

    fun togglePureVegOnly() {
        _filterState.value = _filterState.value.copy(pureVegOnly = !_filterState.value.pureVegOnly)
    }

    fun setSortOption(sort: SortOption) {
        _filterState.value = _filterState.value.copy(sortBy = sort)
    }

    fun setMinRating(rating: Float) {
        _filterState.value = _filterState.value.copy(minRating = rating)
    }

    fun setMaxDeliveryTime(minutes: Int) {
        _filterState.value = _filterState.value.copy(maxDeliveryTimeMinutes = minutes)
    }

    fun setPriceTier(tier: Int) {
        _filterState.value = _filterState.value.copy(maxPriceTier = tier)
    }

    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun resetFilters() {
        _filterState.value = DietaryFilterState(query = _filterState.value.query)
    }

    fun setFilterSheetVisible(visible: Boolean) {
        _isFilterSheetVisible.value = visible
    }

    // Navigation and View Handlers
    fun selectRestaurant(restaurantId: String?) {
        _selectedRestaurantId.value = restaurantId
    }

    fun selectTrackingOrder(orderId: String?) {
        _trackingOrderId.value = orderId
    }

    fun setCustomerTab(tab: CustomerTab) {
        _customerTab.value = tab
        // If navigating to main tabs, clear sub-screen views
        if (_selectedRestaurantId.value != null && tab != CustomerTab.HOME && tab != CustomerTab.EXPLORE) {
            _selectedRestaurantId.value = null
        }
        if (_trackingOrderId.value != null && tab != CustomerTab.ORDERS) {
            _trackingOrderId.value = null
        }
    }

    fun signIn(email: String, password: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            when (val result = withContext(Dispatchers.IO) { authStore.signIn(email, password) }) {
                is AuthResult.Success -> {
                    _authUser.value = result.user
                    _activeUserId.value = result.user.uid
                    _appMode.value = result.user.role
                    onResult(null)
                }
                is AuthResult.Failure -> onResult(result.message)
            }
        }
    }

    fun register(name: String, email: String, password: String, role: AppMode, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            when (val result = withContext(Dispatchers.IO) { authStore.register(name, email, password, role) }) {
                is AuthResult.Success -> {
                    _authUser.value = result.user
                    _activeUserId.value = result.user.uid
                    _appMode.value = result.user.role
                    onResult(null)
                }
                is AuthResult.Failure -> onResult(result.message)
            }
        }
    }

    fun guestSignIn() {
        val guest = authStore.guestSignIn()
        _authUser.value = guest
        _activeUserId.value = guest.uid
        _appMode.value = AppMode.CUSTOMER
    }

    fun signOut() {
        authStore.signOut()
        _authUser.value = null
        _activeUserId.value = null
        _appMode.value = AppMode.CUSTOMER
        _selectedRestaurantId.value = null
        _trackingOrderId.value = null
    }

    fun setAppMode(mode: AppMode) {
        if (_authUser.value?.role == mode) _appMode.value = mode
    }

    // Cart & Order Operations
    fun addToCart(item: MenuItemEntity, dietaryNote: String = "") {
        viewModelScope.launch {
            repository.addToCart(item, dietaryNote)
        }
    }

    fun updateCartQuantity(cartItemId: String, newQty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, newQty)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun applyCoupon(code: String) {
        activeCoupon.value = code.trim().uppercase()
    }

    fun placeOrder(onSuccess: (OrderEntity) -> Unit) {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) return@launch

            val restId = items.first().restaurantId
            val restaurant = allRestaurants.value.find { it.id == restId } ?: return@launch

            val order = repository.placeOrder(
                restaurant = restaurant,
                cartItems = items,
                deliveryAddress = deliveryAddress.value,
                deliveryType = deliveryType.value,
                appliedCoupon = activeCoupon.value,
                chefDietaryNotes = chefDietaryNotes.value
            )

            // Add rewards points for customer order
            rewardPoints.value += (order.totalAmount * 10).toInt()
            _trackingOrderId.value = order.orderId
            _customerTab.value = CustomerTab.ORDERS
            onSuccess(order)
        }
    }

    fun advanceOrderStatus(orderId: String) {
        viewModelScope.launch {
            val order = allOrders.value.find { it.orderId == orderId } ?: return@launch
            val nextStatus = when (order.status) {
                OrderStatus.PLACED -> OrderStatus.ACCEPTED
                OrderStatus.ACCEPTED -> OrderStatus.PREPARING
                OrderStatus.PREPARING -> OrderStatus.READY_FOR_PICKUP
                OrderStatus.READY_FOR_PICKUP -> OrderStatus.ON_THE_WAY
                OrderStatus.ON_THE_WAY -> OrderStatus.DELIVERED
                OrderStatus.DELIVERED -> OrderStatus.DELIVERED
                OrderStatus.CANCELLED -> OrderStatus.CANCELLED
            }
            repository.updateOrderStatus(orderId, nextStatus)
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, OrderStatus.CANCELLED)
        }
    }

    // Reorder Handlers (Single-Click Reorder & Reorder to Cart)
    fun instantReorder(order: OrderEntity, onPlaced: (OrderEntity) -> Unit) {
        viewModelScope.launch {
            val newOrder = repository.instantReorder(order)
            rewardPoints.value += (newOrder.totalAmount * 10).toInt()
            _trackingOrderId.value = newOrder.orderId
            _customerTab.value = CustomerTab.ORDERS
            onPlaced(newOrder)
        }
    }

    fun reorderToCart(order: OrderEntity, onComplete: (itemCount: Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.reorderToCart(order)
            onComplete(count)
        }
    }

    // Reservations
    fun bookReservation(
        restaurant: RestaurantEntity,
        date: String,
        time: String,
        guests: Int,
        dietaryNotes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.bookReservation(restaurant, date, time, guests, dietaryNotes)
            _customerTab.value = CustomerTab.RESERVATIONS
            onSuccess()
        }
    }

    // Favorites
    fun toggleFavorite(restaurantId: String, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val wasSaved = favoriteIds.value.contains(restaurantId)
            repository.toggleFavorite(restaurantId)
            onResult?.invoke(!wasSaved)
        }
    }

    // Partner controls
    fun toggleMenuItemAvailability(menuItemId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.toggleMenuItemAvailability(menuItemId, isAvailable)
        }
    }

    fun toggleRestaurantOpen(restaurantId: String, isOpen: Boolean) {
        viewModelScope.launch {
            repository.updateRestaurantOpenStatus(restaurantId, isOpen)
        }
    }

    // Reviews & Ratings
    fun submitReview(
        restaurantId: String,
        userName: String,
        rating: Float,
        comment: String,
        dietaryTagsUsed: String,
        onSuccess: (ReviewEntity) -> Unit
    ) {
        viewModelScope.launch {
            val review = repository.submitReview(
                restaurantId = restaurantId,
                userName = userName,
                rating = rating,
                comment = comment,
                dietaryTagsUsed = dietaryTagsUsed
            )
            onSuccess(review)
        }
    }
}
