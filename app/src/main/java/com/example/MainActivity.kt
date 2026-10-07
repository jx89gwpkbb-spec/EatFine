package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppMode
import com.example.data.model.OrderStatus
import com.example.ui.components.DietaryFilterBottomSheet
import com.example.ui.components.EatFineBottomNav
import com.example.ui.components.EatFineTopBar
import com.example.ui.components.FloatingCartBar
import com.example.ui.components.RealTimeOrderTrackerCompactBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartCheckoutScreen
import com.example.ui.screens.DeliveryPartnerScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.OrdersListScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReservationsScreen
import com.example.ui.screens.RestaurantDetailScreen
import com.example.ui.screens.RestaurantPartnerDashboard
import com.example.ui.theme.EatFineTheme
import com.example.ui.viewmodel.CustomerTab
import com.example.ui.viewmodel.EatFineViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: EatFineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EatFineTheme {
                EatFineApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EatFineApp(viewModel: EatFineViewModel) {
    val authUser by viewModel.authUser.collectAsStateWithLifecycle()
    val authInitialized by viewModel.authInitialized.collectAsStateWithLifecycle()
    val appMode by viewModel.appMode.collectAsStateWithLifecycle()
    val customerTab by viewModel.customerTab.collectAsStateWithLifecycle()
    val selectedRestaurantId by viewModel.selectedRestaurantId.collectAsStateWithLifecycle()
    val trackingOrderId by viewModel.trackingOrderId.collectAsStateWithLifecycle()
    val isFilterSheetVisible by viewModel.isFilterSheetVisible.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()

    val allRestaurants by viewModel.allRestaurants.collectAsStateWithLifecycle()
    val filteredRestaurants by viewModel.filteredRestaurants.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartItemCount by viewModel.cartItemCount.collectAsStateWithLifecycle()
    val cartSubtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val activeOngoingOrder by viewModel.activeOngoingOrder.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val allReservations by viewModel.allReservations.collectAsStateWithLifecycle()

    val deliveryAddress by viewModel.deliveryAddress.collectAsStateWithLifecycle()
    val deliveryType by viewModel.deliveryType.collectAsStateWithLifecycle()
    val chefDietaryNotes by viewModel.chefDietaryNotes.collectAsStateWithLifecycle()
    val activeCoupon by viewModel.activeCoupon.collectAsStateWithLifecycle()
    val rewardPoints by viewModel.rewardPoints.collectAsStateWithLifecycle()

    val userProfileSettings by viewModel.userProfileSettings.collectAsStateWithLifecycle()
    val adminPlatformSettings by viewModel.adminPlatformSettings.collectAsStateWithLifecycle()
    val businessOwnerSettings by viewModel.businessOwnerSettings.collectAsStateWithLifecycle()
    val deliveryAgentSettings by viewModel.deliveryAgentSettings.collectAsStateWithLifecycle()
    val allPlatformUsers by viewModel.allPlatformUsers.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isCartOpen by remember { mutableStateOf(false) }

    val activeOrdersCount = remember(allOrders) {
        allOrders.count { it.status.stepIndex in 0..4 }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = if (authUser != null) {
            {
            EatFineTopBar(
                address = deliveryAddress,
                currentMode = appMode,
                onSignOut = { viewModel.signOut() },
                activeDietaryCount = filterState.selectedRestrictions.size,
                onOpenDietaryFilter = { viewModel.setFilterSheetVisible(true) }
            )
            }
        } else {
            {}
        },
        bottomBar = {
            if (authInitialized && authUser != null && appMode == AppMode.CUSTOMER && trackingOrderId == null && selectedRestaurantId == null && !isCartOpen) {
                EatFineBottomNav(
                    currentTab = customerTab,
                    onTabSelected = { viewModel.setCustomerTab(it) },
                    activeOrdersCount = activeOrdersCount,
                    favoritesCount = favoriteIds.size
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!authInitialized) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (authUser == null) {
                AuthScreen(
                    onSignIn = { email, password, onResult -> viewModel.signIn(email, password, onResult) },
                    onRegister = { name, email, password, onResult ->
                        viewModel.register(name, email, password, AppMode.CUSTOMER, onResult)
                    },
                    onResetPassword = { email, onResult -> viewModel.resetPassword(email, onResult) },
                    onGuestSignIn = { viewModel.guestSignIn() }
                )
            } else when (appMode) {
                AppMode.CUSTOMER -> {
                    when {
                        // 1. Live Order Tracking View
                        trackingOrderId != null -> {
                            val activeOrder = allOrders.find { it.orderId == trackingOrderId }
                            if (activeOrder != null) {
                                OrderTrackingScreen(
                                    order = activeOrder,
                                    onBack = { viewModel.selectTrackingOrder(null) },
                                    onAdvanceStatus = { viewModel.advanceOrderStatus(it) },
                                    onCancelOrder = { viewModel.cancelOrder(it) }
                                )
                            } else {
                                viewModel.selectTrackingOrder(null)
                            }
                        }

                        // 2. Cart & Checkout View
                        isCartOpen -> {
                            val cartRestaurant = allRestaurants.find { it.id == cartItems.firstOrNull()?.restaurantId }
                            CartCheckoutScreen(
                                cartItems = cartItems,
                                restaurant = cartRestaurant,
                                deliveryAddress = deliveryAddress,
                                onAddressChange = { viewModel.deliveryAddress.value = it },
                                deliveryType = deliveryType,
                                onDeliveryTypeChange = { viewModel.deliveryType.value = it },
                                activeCoupon = activeCoupon,
                                onApplyCoupon = { viewModel.applyCoupon(it) },
                                chefDietaryNotes = chefDietaryNotes,
                                onChefNotesChange = { viewModel.chefDietaryNotes.value = it },
                                onUpdateQuantity = { id, qty -> viewModel.updateCartQuantity(id, qty) },
                                onClearCart = { viewModel.clearCart() },
                                onPlaceOrder = {
                                    viewModel.placeOrder { order ->
                                        isCartOpen = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Order #${order.orderId} Placed! Tracking live delivery.")
                                        }
                                    }
                                },
                                onBack = { isCartOpen = false }
                            )
                        }

                        // 3. Restaurant Detail View
                        selectedRestaurantId != null -> {
                            val rest = allRestaurants.find { it.id == selectedRestaurantId }
                            if (rest != null) {
                                val menuItems by viewModel.getMenuItemsForRestaurant(rest.id)
                                    .collectAsState(initial = emptyList())
                                val reviews by viewModel.getReviewsForRestaurant(rest.id)
                                    .collectAsState(initial = emptyList())

                                RestaurantDetailScreen(
                                    restaurant = rest,
                                    menuItems = menuItems,
                                    cartItems = cartItems,
                                    isFavorite = rest.id in favoriteIds,
                                    onBack = { viewModel.selectRestaurant(null) },
                                    onToggleFavorite = {
                                        viewModel.toggleFavorite(rest.id) { isSaved ->
                                            scope.launch {
                                                val msg = if (isSaved) "❤️ Added ${rest.name} to Favorites" else "Removed from Favorites"
                                                snackbarHostState.showSnackbar(msg)
                                            }
                                        }
                                    },
                                    onAddToCart = { item ->
                                        viewModel.addToCart(item)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Added ${item.name} to cart")
                                        }
                                    },
                                    onUpdateQuantity = { id, qty -> viewModel.updateCartQuantity(id, qty) },
                                    onBookTable = {
                                        viewModel.setCustomerTab(CustomerTab.RESERVATIONS)
                                        viewModel.selectRestaurant(null)
                                    },
                                    reviews = reviews,
                                    onSubmitReview = { rating, comment, dietaryTags, userName ->
                                        viewModel.submitReview(
                                            restaurantId = rest.id,
                                            userName = userName,
                                            rating = rating,
                                            comment = comment,
                                            dietaryTagsUsed = dietaryTags
                                        ) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Review submitted! Rating updated for ${rest.name}")
                                            }
                                        }
                                    }
                                )
                            } else {
                                viewModel.selectRestaurant(null)
                            }
                        }

                        // 4. Main Tab Navigation
                        else -> {
                            when (customerTab) {
                                CustomerTab.HOME, CustomerTab.EXPLORE -> {
                                    HomeScreen(
                                        restaurants = filteredRestaurants,
                                        favoriteIds = favoriteIds,
                                        filterState = filterState,
                                        onToggleDietaryRestriction = { viewModel.toggleDietaryRestriction(it) },
                                        pureVegOnly = filterState.pureVegOnly,
                                        onTogglePureVeg = { viewModel.togglePureVegOnly() },
                                        onOpenFilterSheet = { viewModel.setFilterSheetVisible(true) },
                                        onResetFilters = { viewModel.resetFilters() },
                                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                        onSelectRestaurant = { viewModel.selectRestaurant(it) },
                                        onToggleFavorite = { id ->
                                            val rest = allRestaurants.find { it.id == id }
                                            viewModel.toggleFavorite(id) { isSaved ->
                                                scope.launch {
                                                    val msg = if (isSaved) "❤️ Added ${rest?.name ?: "Restaurant"} to Favorites" else "Removed from Favorites"
                                                    snackbarHostState.showSnackbar(msg)
                                                }
                                            }
                                        },
                                        onNavigateExplore = { viewModel.setCustomerTab(CustomerTab.EXPLORE) },
                                        activeOngoingOrder = activeOngoingOrder,
                                        onTrackOrder = { viewModel.selectTrackingOrder(it) },
                                        onAdvanceOrderStatus = { viewModel.advanceOrderStatus(it) }
                                    )
                                }

                                CustomerTab.ORDERS -> {
                                    OrdersListScreen(
                                        orders = allOrders,
                                        onSelectOrder = { viewModel.selectTrackingOrder(it) },
                                        onAdvanceOrderStatus = { viewModel.advanceOrderStatus(it) },
                                        onRateRestaurant = { viewModel.selectRestaurant(it) },
                                        onInstantReorder = { order ->
                                            viewModel.instantReorder(order) { newOrder ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("⚡ 1-Click Reorder placed! #${newOrder.orderId} from ${order.restaurantName}")
                                                }
                                            }
                                        },
                                        onReorderToCart = { order ->
                                            viewModel.reorderToCart(order) { itemCount ->
                                                isCartOpen = true
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("🛒 Loaded $itemCount items from ${order.restaurantName} into your cart")
                                                }
                                            }
                                        },
                                        onExploreRestaurants = { viewModel.setCustomerTab(CustomerTab.EXPLORE) }
                                    )
                                }

                                CustomerTab.FAVORITES -> {
                                    val favRestaurants = allRestaurants.filter { it.id in favoriteIds }
                                    FavoritesScreen(
                                        favorites = favRestaurants,
                                        onSelectRestaurant = { viewModel.selectRestaurant(it) },
                                        onToggleFavorite = { id ->
                                            val rest = allRestaurants.find { it.id == id }
                                            viewModel.toggleFavorite(id) { isSaved ->
                                                scope.launch {
                                                    val msg = if (isSaved) "❤️ Added ${rest?.name ?: "Restaurant"} to Favorites" else "Removed from Favorites"
                                                    snackbarHostState.showSnackbar(msg)
                                                }
                                            }
                                        },
                                        onExploreClick = { viewModel.setCustomerTab(CustomerTab.EXPLORE) }
                                    )
                                }

                                CustomerTab.RESERVATIONS -> {
                                    ReservationsScreen(
                                        reservations = allReservations,
                                        restaurants = allRestaurants,
                                        onBookReservation = { r, date, time, guests, notes ->
                                            viewModel.bookReservation(r, date, time, guests, notes) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Table reserved at ${r.name}!")
                                                }
                                            }
                                        }
                                    )
                                }

                                CustomerTab.PROFILE -> {
                                    ProfileScreen(
                                        rewardPoints = rewardPoints,
                                        address = deliveryAddress,
                                        accountName = authUser?.name.orEmpty(),
                                        accountEmail = authUser?.email.orEmpty(),
                                        accountRole = authUser?.role ?: AppMode.CUSTOMER,
                                        restaurantId = authUser?.restaurantId,
                                        driverId = authUser?.driverId,
                                        userSettings = userProfileSettings,
                                        onSaveUserSettings = { viewModel.updateUserProfileSettings(it) },
                                        onSignOut = { viewModel.signOut() },
                                        onSelectMode = { viewModel.setAppMode(it) }
                                    )
                                }
                            }
                        }
                    }

                    // Floating Cart Bar (visible in Home/Explore/Detail when not in cart)
                    if (!isCartOpen && trackingOrderId == null && cartItemCount > 0) {
                        FloatingCartBar(
                            itemCount = cartItemCount,
                            subtotal = cartSubtotal,
                            onViewCart = { isCartOpen = true },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = if (selectedRestaurantId != null) 16.dp else 4.dp)
                        )
                    } else if (!isCartOpen && trackingOrderId == null && activeOngoingOrder != null && customerTab != CustomerTab.HOME && customerTab != CustomerTab.ORDERS) {
                        RealTimeOrderTrackerCompactBar(
                            order = activeOngoingOrder!!,
                            onTrackClick = { id -> viewModel.selectTrackingOrder(id) },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 16.dp, vertical = if (selectedRestaurantId != null) 16.dp else 6.dp)
                        )
                    }
                }

                // RESTAURANT PARTNER VIEW (Scoped to assigned business restaurant & orders)
                AppMode.RESTAURANT_PARTNER -> {
                    val partnerRest = allRestaurants.find { it.id == authUser?.restaurantId }
                        ?: allRestaurants.firstOrNull()
                    if (partnerRest != null) {
                        val menuItems by viewModel.getMenuItemsForRestaurant(partnerRest.id)
                            .collectAsState(initial = emptyList())
                        val restaurantOrders = allOrders.filter { it.restaurantId == partnerRest.id }
                        RestaurantPartnerDashboard(
                            restaurant = partnerRest,
                            orders = restaurantOrders,
                            menuItems = menuItems,
                            businessSettings = businessOwnerSettings,
                            onUpdateBusinessSettings = { viewModel.updateBusinessOwnerSettings(it) },
                            onAdvanceOrderStatus = { viewModel.advanceOrderStatus(it) },
                            onToggleItemAvailability = { id, avail -> viewModel.toggleMenuItemAvailability(id, avail) },
                            onToggleOpen = { viewModel.toggleRestaurantOpen(partnerRest.id, it) },
                            onSwitchMode = { viewModel.setAppMode(it) }
                        )
                    }
                }

                // DELIVERY PARTNER VIEW (Scoped to driver profile and dispatched delivery orders)
                AppMode.DELIVERY_PARTNER -> {
                    val driverOrders = allOrders.filter {
                        it.status in listOf(
                            OrderStatus.ACCEPTED,
                            OrderStatus.PREPARING,
                            OrderStatus.READY_FOR_PICKUP,
                            OrderStatus.ON_THE_WAY
                        )
                    }
                    DeliveryPartnerScreen(
                        orders = driverOrders,
                        deliverySettings = deliveryAgentSettings,
                        onUpdateDeliverySettings = { viewModel.updateDeliveryAgentSettings(it) },
                        onAdvanceOrderStatus = { viewModel.advanceOrderStatus(it) },
                        onSwitchMode = { viewModel.setAppMode(it) }
                    )
                }

                // ADMIN VIEW
                AppMode.ADMIN -> {
                    AdminDashboardScreen(
                        orders = allOrders,
                        restaurants = allRestaurants,
                        platformSettings = adminPlatformSettings,
                        onUpdatePlatformSettings = { viewModel.updateAdminPlatformSettings(it) },
                        users = allPlatformUsers,
                        onCreateUser = { name, email, pass, role, restId, driverId, onDone ->
                            viewModel.adminCreateUser(name, email, pass, role, restId, driverId, onDone)
                        },
                        onUpdateUserRole = { uid, role, restId, driverId ->
                            viewModel.adminUpdateUserRole(uid, role, restId, driverId)
                        },
                        onSwitchMode = { viewModel.setAppMode(it) }
                    )
                }
            }

            // Dietary Filter Bottom Sheet
            if (isFilterSheetVisible) {
                DietaryFilterBottomSheet(
                    filterState = filterState,
                    onToggleRestriction = { viewModel.toggleDietaryRestriction(it) },
                    onTogglePureVeg = { viewModel.togglePureVegOnly() },
                    onSetMatchAll = { viewModel.setMatchAllDietary(it) },
                    onSetSort = { viewModel.setSortOption(it) },
                    onSetMinRating = { viewModel.setMinRating(it) },
                    onSetMaxDeliveryTime = { viewModel.setMaxDeliveryTime(it) },
                    onReset = { viewModel.resetFilters() },
                    matchingCount = filteredRestaurants.size,
                    onDismiss = { viewModel.setFilterSheetVisible(false) }
                )
            }
        }
    }
}
