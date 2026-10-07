package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthUser
import com.example.data.model.AdminPlatformSettings
import com.example.data.model.AppMode
import com.example.data.model.BusinessOwnerSettings
import com.example.data.model.DeliveryAgentSettings
import com.example.data.model.MenuItemEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.RestaurantEntity
import com.example.ui.components.VegNonVegSymbol
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.FreshGreen

// =========================================================================
// 1. RESTAURANT PARTNER / BUSINESS OWNER DASHBOARD
// =========================================================================
@Composable
fun RestaurantPartnerDashboard(
    restaurant: RestaurantEntity,
    orders: List<OrderEntity>,
    menuItems: List<MenuItemEntity>,
    businessSettings: BusinessOwnerSettings = BusinessOwnerSettings(),
    onUpdateBusinessSettings: ((BusinessOwnerSettings) -> Unit)? = null,
    onAdvanceOrderStatus: (String) -> Unit,
    onToggleItemAvailability: (String, Boolean) -> Unit,
    onToggleOpen: (Boolean) -> Unit,
    onSwitchMode: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val pendingOrders = orders.filter { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("partner_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Kitchen Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = restaurant.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = FreshGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Dietary Verified Partner Kitchen",
                                fontSize = 12.sp,
                                color = FreshGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (restaurant.isOpen) "Open" else "Closed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (restaurant.isOpen) FreshGreen else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = restaurant.isOpen,
                                onCheckedChange = { onToggleOpen(it) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(title = "Today's Orders", value = "${orders.size + 14}", modifier = Modifier.weight(1f))
                        MetricCard(title = "Kitchen Revenue", value = "$${"%.2f".format(orders.sumOf { it.totalAmount } + 420.0)}", modifier = Modifier.weight(1f))
                        MetricCard(title = "Dietary Score", value = "99.4%", modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Tabs: Live Orders, Menu, and Business Settings
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Orders (${pendingOrders.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Menu (${menuItems.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Live Orders
                if (pendingOrders.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🍳", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Kitchen is all caught up!", fontWeight = FontWeight.Bold)
                            Text(text = "New orders will chime with dietary safety instructions.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(pendingOrders, key = { it.orderId }) { order ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.2.dp, BrandOrange.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Order #${order.orderId}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Surface(
                                        color = BrandOrange,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = order.status.display,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = order.itemsSummary, fontSize = 13.sp, fontWeight = FontWeight.Medium)

                                if (order.chefDietaryInstructions.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = FreshGreen.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = "Dietary notice",
                                                tint = FreshGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "DIETARY SAFETY INSTRUCTION:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = FreshGreen
                                                )
                                                Text(
                                                    text = order.chefDietaryInstructions,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { onAdvanceOrderStatus(order.orderId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = when (order.status) {
                                            OrderStatus.PLACED -> "Accept & Send to Kitchen"
                                            OrderStatus.ACCEPTED -> "Start Food Preparation"
                                            OrderStatus.PREPARING -> "Mark Ready for Delivery Driver"
                                            else -> "Next Status"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Menu Item Stock & Availability
                item {
                    Text(
                        text = "Toggle items out of stock if dietary ingredients are temporarily unavailable.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(menuItems, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                VegNonVegSymbol(isVeg = item.isVeg)
                                Column {
                                    Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "$${"%.2f".format(item.price)} • ${item.dietaryRestrictionsCsv}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Switch(
                                checked = item.isAvailable,
                                onCheckedChange = { onToggleItemAvailability(item.id, it) }
                            )
                        }
                    }
                }
            }

            2 -> {
                // Business Owner Settings Tab
                item {
                    BusinessSettingsSection(
                        settings = businessSettings,
                        onSave = { onUpdateBusinessSettings?.invoke(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BusinessSettingsSection(
    settings: BusinessOwnerSettings,
    onSave: (BusinessOwnerSettings) -> Unit
) {
    var name by remember(settings) { mutableStateOf(settings.restaurantName) }
    var cuisines by remember(settings) { mutableStateOf(settings.cuisines) }
    var phone by remember(settings) { mutableStateOf(settings.phoneNumber) }
    var address by remember(settings) { mutableStateOf(settings.address) }
    var hours by remember(settings) { mutableStateOf(settings.operatingHours) }
    var isOpen by remember(settings) { mutableStateOf(settings.isOpen) }
    var isBusySurge by remember(settings) { mutableStateOf(settings.isKitchenBusySurge) }
    var autoAccept by remember(settings) { mutableStateOf(settings.autoAcceptOrders) }
    var prepMinutes by remember(settings) { mutableIntStateOf(settings.defaultPrepMinutes) }
    var minOrder by remember(settings) { mutableDoubleStateOf(settings.minimumOrderValue) }
    var deliveryRadius by remember(settings) { mutableDoubleStateOf(settings.deliveryRadiusMiles) }
    var dedicatedFryer by remember(settings) { mutableStateOf(settings.hasDedicatedFryer) }
    var allergenCertified by remember(settings) { mutableStateOf(settings.allergenCrossContaminationCertified) }
    var bankAccount by remember(settings) { mutableStateOf(settings.bankAccountMasked) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Operational Toggles Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "KITCHEN OPERATIONS & LIVE STATUS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandOrange
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Accepting Orders (Kitchen Open)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("When paused, diners cannot place new orders", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isOpen, onCheckedChange = { isOpen = it })
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kitchen Busy Surge (+15m buffer)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Extends customer delivery estimate during rush hour", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isBusySurge, onCheckedChange = { isBusySurge = it })
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Accept Incoming Orders", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Instantly send orders to kitchen prep station", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = autoAccept, onCheckedChange = { autoAccept = it })
                }
            }
        }

        // Restaurant Profile Details Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "RESTAURANT PROFILE & TIMINGS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandOrange
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Restaurant Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cuisines,
                    onValueChange = { cuisines = it },
                    label = { Text("Cuisines & Specialties") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Street Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Operating Hours") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = prepMinutes.toString(),
                        onValueChange = { prepMinutes = it.toIntOrNull() ?: prepMinutes },
                        label = { Text("Prep Time (min)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minOrder.toString(),
                        onValueChange = { minOrder = it.toDoubleOrNull() ?: minOrder },
                        label = { Text("Min Order ($)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Dietary & Cross-Contamination Protocols Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "DIETARY & ALLERGEN CERTIFICATIONS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = FreshGreen
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dedicated Gluten-Free Fryer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Prevents oil cross-contact for celiac diners", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = dedicatedFryer, onCheckedChange = { dedicatedFryer = it })
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allergen Separation Protocol Certified", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Kitchen sanitized surfaces per EatFine standard", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = allergenCertified, onCheckedChange = { allergenCertified = it })
                }
            }
        }

        // Banking & Payouts Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "PAYOUT & BANKING SETTINGS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandOrange
                )

                OutlinedTextField(
                    value = bankAccount,
                    onValueChange = { bankAccount = it },
                    label = { Text("Payout Account") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Automatic daily deposits for all completed orders.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        saveSuccessMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = FreshGreen.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    color = FreshGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Save Button
        Button(
            onClick = {
                val updated = settings.copy(
                    restaurantName = name,
                    cuisines = cuisines,
                    phoneNumber = phone,
                    address = address,
                    operatingHours = hours,
                    isOpen = isOpen,
                    isKitchenBusySurge = isBusySurge,
                    autoAcceptOrders = autoAccept,
                    defaultPrepMinutes = prepMinutes,
                    minimumOrderValue = minOrder,
                    hasDedicatedFryer = dedicatedFryer,
                    allergenCrossContaminationCertified = allergenCertified,
                    bankAccountMasked = bankAccount
                )
                onSave(updated)
                saveSuccessMessage = "✓ Restaurant settings saved successfully!"
            },
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_business_settings_button")
        ) {
            Text("Save Business Settings", fontWeight = FontWeight.Bold)
        }
    }
}

// =========================================================================
// 2. DELIVERY PARTNER / AGENT DASHBOARD
// =========================================================================
@Composable
fun DeliveryPartnerScreen(
    orders: List<OrderEntity>,
    deliverySettings: DeliveryAgentSettings = DeliveryAgentSettings(),
    onUpdateDeliverySettings: ((DeliveryAgentSettings) -> Unit)? = null,
    onAdvanceOrderStatus: (String) -> Unit,
    onSwitchMode: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isOnline by remember(deliverySettings) { mutableStateOf(deliverySettings.isOnline) }
    val activeDeliveries = orders.filter { it.status == OrderStatus.READY_FOR_PICKUP || it.status == OrderStatus.ON_THE_WAY }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("delivery_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Driver Status Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(FreshGreen.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.DeliveryDining, contentDescription = "Driver", tint = FreshGreen)
                            }
                            Column {
                                Text(text = deliverySettings.driverName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    text = if (isOnline) "🟢 Online & Ready for Orders" else "⚪ Offline",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isOnline) FreshGreen else Color.Gray
                                )
                            }
                        }
                        Switch(
                            checked = isOnline,
                            onCheckedChange = {
                                isOnline = it
                                onUpdateDeliverySettings?.invoke(deliverySettings.copy(isOnline = it))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(title = "Today's Earnings", value = "$78.40", modifier = Modifier.weight(1f))
                        MetricCard(title = "Deliveries", value = "9 completed", modifier = Modifier.weight(1f))
                        MetricCard(title = "Driver Rating", value = "★ 4.95", modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Tabs: Active Deliveries vs Driver Settings
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active Deliveries (${activeDeliveries.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Driver Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        if (selectedTab == 0) {
            // Deliveries List
            if (activeDeliveries.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🛵", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "No active pickup requests right now", fontWeight = FontWeight.Bold)
                            Text(text = "Stay online in high-demand dietary kitchen zones.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(activeDeliveries, key = { it.orderId }) { order ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.2.dp, FreshGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Order #${order.orderId}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "$${"%.2f".format(order.deliveryFee + 3.0)} Payout", color = FreshGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "🏬 Pickup: ${order.restaurantName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "🏠 Drop: ${order.deliveryAddress}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "Items: ${order.itemsSummary}", fontSize = 12.sp)

                            if (order.chefDietaryInstructions.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "⚠️ Special Handling: ${order.chefDietaryInstructions}", fontSize = 11.sp, color = FreshGreen, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { onAdvanceOrderStatus(order.orderId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (order.status == OrderStatus.READY_FOR_PICKUP) BrandOrange else FreshGreen
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (order.status == OrderStatus.READY_FOR_PICKUP) "Confirm Pickup from Restaurant" else "Confirm Drop-off & Delivered",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Driver Settings Tab
            item {
                DeliverySettingsSection(
                    settings = deliverySettings,
                    onSave = { onUpdateDeliverySettings?.invoke(it) }
                )
            }
        }
    }
}

@Composable
private fun DeliverySettingsSection(
    settings: DeliveryAgentSettings,
    onSave: (DeliveryAgentSettings) -> Unit
) {
    var name by remember(settings) { mutableStateOf(settings.driverName) }
    var phone by remember(settings) { mutableStateOf(settings.driverPhone) }
    var vehicleType by remember(settings) { mutableStateOf(settings.vehicleType) }
    var plate by remember(settings) { mutableStateOf(settings.vehiclePlate) }
    var autoAccept by remember(settings) { mutableStateOf(settings.autoAcceptDeliveries) }
    var maxRadius by remember(settings) { mutableDoubleStateOf(settings.maxDeliveryRadiusMiles) }
    var thermalBag by remember(settings) { mutableStateOf(settings.thermalBagVerified) }
    var navApp by remember(settings) { mutableStateOf(settings.navigationApp) }
    var earningsTarget by remember(settings) { mutableDoubleStateOf(settings.dailyEarningsTarget) }
    var payoutAccount by remember(settings) { mutableStateOf(settings.payoutAccountMasked) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Vehicle & Driver Profile Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "DRIVER PROFILE & VEHICLE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandOrange
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Driver Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Driver Contact Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = vehicleType,
                        onValueChange = { vehicleType = it },
                        label = { Text("Vehicle Type") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = plate,
                        onValueChange = { plate = it },
                        label = { Text("Plate / ID") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Dispatch Preferences Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "DISPATCH & DELIVERY PREFERENCES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandOrange
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Accept Nearby Orders", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Automatically claim orders within your radius", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = autoAccept, onCheckedChange = { autoAccept = it })
                }

                HorizontalDivider()

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Max Delivery Radius", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("${"%.1f".format(maxRadius)} miles", fontWeight = FontWeight.Bold, color = BrandOrange)
                    }
                    Slider(
                        value = maxRadius.toFloat(),
                        onValueChange = { maxRadius = it.toDouble() },
                        valueRange = 2f..20f
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Thermal Insulated Food Bag", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Required for food temperature & safety compliance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = thermalBag, onCheckedChange = { thermalBag = it })
                }
            }
        }

        // Payout Settings Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "EARNINGS TARGET & CASH-OUT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = FreshGreen
                )

                OutlinedTextField(
                    value = earningsTarget.toString(),
                    onValueChange = { earningsTarget = it.toDoubleOrNull() ?: earningsTarget },
                    label = { Text("Daily Earnings Target ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = payoutAccount,
                    onValueChange = { payoutAccount = it },
                    label = { Text("Instant Cashout Account") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        saveSuccessMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = FreshGreen.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    color = FreshGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Button(
            onClick = {
                val updated = settings.copy(
                    driverName = name,
                    driverPhone = phone,
                    vehicleType = vehicleType,
                    vehiclePlate = plate,
                    autoAcceptDeliveries = autoAccept,
                    maxDeliveryRadiusMiles = maxRadius,
                    thermalBagVerified = thermalBag,
                    dailyEarningsTarget = earningsTarget,
                    payoutAccountMasked = payoutAccount
                )
                onSave(updated)
                saveSuccessMessage = "✓ Driver settings updated successfully!"
            },
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_delivery_settings_button")
        ) {
            Text("Save Driver Settings", fontWeight = FontWeight.Bold)
        }
    }
}

// =========================================================================
// 3. ADMIN DASHBOARD & USER PROVISIONING SCREEN
// =========================================================================
@Composable
fun AdminDashboardScreen(
    orders: List<OrderEntity>,
    restaurants: List<RestaurantEntity>,
    platformSettings: AdminPlatformSettings = AdminPlatformSettings(),
    onUpdatePlatformSettings: ((AdminPlatformSettings) -> Unit)? = null,
    users: List<AuthUser> = emptyList(),
    onCreateUser: ((String, String, String, AppMode, String?, String?, (String?) -> Unit) -> Unit)? = null,
    onUpdateUserRole: ((String, AppMode, String?, String?) -> Unit)? = null,
    onSwitchMode: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateUserDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "EatFine Admin Central",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "User provisioning, platform commission rules and dietary ecosystem governance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tabs: Analytics, User Provisioning, Platform Settings, Audits
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Analytics", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Users (${users.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Safety Audit", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Key Platform Metrics
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "PLATFORM REVENUE & PERFORMANCE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandOrange
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MetricCard(title = "Gross GMV", value = "$${"%.2f".format(orders.sumOf { it.totalAmount } + 12840.0)}", modifier = Modifier.weight(1f))
                                MetricCard(title = "Total Orders", value = "${orders.size + 842}", modifier = Modifier.weight(1f))
                                MetricCard(title = "Commission Rate", value = "${platformSettings.platformCommissionPercent}%", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            1 -> {
                // User Provisioning & Directory
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REGISTERED USERS & PARTNERS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandOrange
                        )

                        Button(
                            onClick = { showCreateUserDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("admin_add_user_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Create User", fontSize = 12.sp)
                        }
                    }
                }

                items(users, key = { it.uid }) { user ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        color = when (user.role) {
                                            AppMode.ADMIN -> BrandOrange.copy(alpha = 0.2f)
                                            AppMode.RESTAURANT_PARTNER -> FreshGreen.copy(alpha = 0.2f)
                                            AppMode.DELIVERY_PARTNER -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                            AppMode.CUSTOMER -> Color.Gray.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = user.role.name,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (user.role) {
                                                AppMode.ADMIN -> BrandOrange
                                                AppMode.RESTAURANT_PARTNER -> FreshGreen
                                                AppMode.DELIVERY_PARTNER -> Color(0xFF1D4ED8)
                                                AppMode.CUSTOMER -> Color.DarkGray
                                            },
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(text = user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (user.restaurantId != null) {
                                    Text(text = "Linked Restaurant: ${user.restaurantId}", fontSize = 11.sp, color = FreshGreen)
                                }
                                if (user.driverId != null) {
                                    Text(text = "Linked Driver Profile: ${user.driverId}", fontSize = 11.sp, color = Color(0xFF1D4ED8))
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Admin Platform Settings
                item {
                    AdminSettingsSection(
                        settings = platformSettings,
                        onSave = { onUpdatePlatformSettings?.invoke(it) }
                    )
                }
            }

            3 -> {
                // Dietary Safety Registry
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "DIETARY RESTRICTION AUDIT REGISTRY",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = FreshGreen
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            restaurants.forEach { r ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = r.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = r.certificationNote, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        color = FreshGreen.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "VERIFIED",
                                            color = FreshGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Admin Create User
    if (showCreateUserDialog) {
        AdminCreateUserDialog(
            restaurants = restaurants,
            onDismiss = { showCreateUserDialog = false },
            onCreate = { name, email, pass, role, restId, driverId, onDone ->
                onCreateUser?.invoke(name, email, pass, role, restId, driverId) { err ->
                    onDone(err)
                    if (err == null) showCreateUserDialog = false
                }
            }
        )
    }
}

@Composable
private fun AdminSettingsSection(
    settings: AdminPlatformSettings,
    onSave: (AdminPlatformSettings) -> Unit
) {
    var commission by remember(settings) { mutableDoubleStateOf(settings.platformCommissionPercent) }
    var baseFee by remember(settings) { mutableDoubleStateOf(settings.baseDeliveryFee) }
    var surgeMultiplier by remember(settings) { mutableDoubleStateOf(settings.surgeDeliveryMultiplier) }
    var isPlatformOpen by remember(settings) { mutableStateOf(settings.isPlatformOpen) }
    var announcement by remember(settings) { mutableStateOf(settings.systemAnnouncement) }
    var requireAudit by remember(settings) { mutableStateOf(settings.requireDietaryAuditForListing) }
    var autoAssign by remember(settings) { mutableStateOf(settings.autoAssignDrivers) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "PLATFORM MONETIZATION & FEES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandOrange
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Platform Restaurant Commission", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("${commission.toInt()}%", fontWeight = FontWeight.Bold, color = BrandOrange)
                    }
                    Slider(
                        value = commission.toFloat(),
                        onValueChange = { commission = it.toDouble() },
                        valueRange = 5f..30f
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = baseFee.toString(),
                        onValueChange = { baseFee = it.toDoubleOrNull() ?: baseFee },
                        label = { Text("Base Delivery Fee ($)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = surgeMultiplier.toString(),
                        onValueChange = { surgeMultiplier = it.toDoubleOrNull() ?: surgeMultiplier },
                        label = { Text("Surge Multiplier (x)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "GLOBAL PLATFORM GOVERNANCE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandOrange
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Master Platform Operational Status", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Emergency pause stops all incoming checkouts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isPlatformOpen, onCheckedChange = { isPlatformOpen = it })
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Strict Dietary Audit Enforced", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Block uncertified restaurant menus automatically", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = requireAudit, onCheckedChange = { requireAudit = it })
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = announcement,
                    onValueChange = { announcement = it },
                    label = { Text("System Broadcast Announcement") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        saveMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = FreshGreen.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    color = FreshGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Button(
            onClick = {
                val updated = settings.copy(
                    platformCommissionPercent = commission,
                    baseDeliveryFee = baseFee,
                    surgeDeliveryMultiplier = surgeMultiplier,
                    isPlatformOpen = isPlatformOpen,
                    systemAnnouncement = announcement,
                    requireDietaryAuditForListing = requireAudit,
                    autoAssignDrivers = autoAssign
                )
                onSave(updated)
                saveMessage = "✓ Admin platform settings saved successfully!"
            },
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_admin_settings_button")
        ) {
            Text("Save Platform Settings", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AdminCreateUserDialog(
    restaurants: List<RestaurantEntity>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, AppMode, String?, String?, (String?) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(AppMode.CUSTOMER) }
    var selectedRestaurantId by remember { mutableStateOf(restaurants.firstOrNull()?.id.orEmpty()) }
    var driverId by remember { mutableStateOf("driver_1") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Provision New User Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Temporary Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Assigned Role:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    AppMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedRole == mode,
                            onClick = { selectedRole = mode },
                            label = { Text(mode.name.take(4), fontSize = 10.sp) }
                        )
                    }
                }

                if (selectedRole == AppMode.RESTAURANT_PARTNER) {
                    OutlinedTextField(
                        value = selectedRestaurantId,
                        onValueChange = { selectedRestaurantId = it },
                        label = { Text("Associated Restaurant ID (e.g. rest_1)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedRole == AppMode.DELIVERY_PARTNER) {
                    OutlinedTextField(
                        value = driverId,
                        onValueChange = { driverId = it },
                        label = { Text("Driver Profile ID (e.g. driver_1)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    isSubmitting = true
                    errorMessage = null
                    val restParam = if (selectedRole == AppMode.RESTAURANT_PARTNER) selectedRestaurantId else null
                    val driverParam = if (selectedRole == AppMode.DELIVERY_PARTNER) driverId else null
                    onCreate(name, email, password, selectedRole, restParam, driverParam) { err ->
                        isSubmitting = false
                        errorMessage = err
                    }
                }
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(6.dp))
                }
                Text("Create User")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
