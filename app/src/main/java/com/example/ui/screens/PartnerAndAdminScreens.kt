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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.AppMode
import com.example.data.model.MenuItemEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.RestaurantEntity
import com.example.ui.components.VegNonVegSymbol
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.FreshGreen

// RESTAURANT PARTNER DASHBOARD
@Composable
fun RestaurantPartnerDashboard(
    restaurant: RestaurantEntity,
    orders: List<OrderEntity>,
    menuItems: List<MenuItemEntity>,
    onAdvanceOrderStatus: (String) -> Unit,
    onToggleItemAvailability: (String, Boolean) -> Unit,
    onToggleOpen: (Boolean) -> Unit,
    onSwitchMode: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
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

        // Tabs: Live Orders vs Menu Controls
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Live Kitchen Orders (${pendingOrders.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Menu & Stock (${menuItems.size})", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedTab == 0) {
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

                            // Dietary alert banner
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
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Button
                            val nextActionLabel = when (order.status) {
                                OrderStatus.PLACED -> "Accept & Send to Kitchen"
                                OrderStatus.ACCEPTED -> "Start Cooking"
                                OrderStatus.PREPARING -> "Mark Food Ready for Driver"
                                OrderStatus.READY_FOR_PICKUP -> "Waiting for Driver Pickup"
                                OrderStatus.ON_THE_WAY -> "Driver on the way"
                                OrderStatus.DELIVERED -> "Completed"
                                OrderStatus.CANCELLED -> "Cancelled"
                            }

                            if (order.status != OrderStatus.READY_FOR_PICKUP && order.status != OrderStatus.ON_THE_WAY) {
                                Button(
                                    onClick = { onAdvanceOrderStatus(order.orderId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = nextActionLabel, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Menu Items Stock Management
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                VegNonVegSymbol(isVeg = item.isVeg)
                                Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(text = "$${"%.2f".format(item.price)} • ${item.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (item.isAvailable) "In Stock" else "Sold Out",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isAvailable) FreshGreen else Color.Red
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = item.isAvailable,
                                onCheckedChange = { onToggleItemAvailability(item.id, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// DELIVERY PARTNER SCREEN
@Composable
fun DeliveryPartnerScreen(
    orders: List<OrderEntity>,
    onAdvanceOrderStatus: (String) -> Unit,
    onSwitchMode: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var isOnline by remember { mutableStateOf(true) }
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
                                Text(text = "Alex Rivera", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    text = if (isOnline) "🟢 Online & Ready for Orders" else "⚪ Offline",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isOnline) FreshGreen else Color.Gray
                                )
                            }
                        }
                        Switch(checked = isOnline, onCheckedChange = { isOnline = it })
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

        // Active Deliveries Section
        item {
            Text(
                text = "ASSIGNED DELIVERIES (${activeDeliveries.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = BrandOrange
            )
        }

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
    }
}

// ADMIN DASHBOARD SCREEN
@Composable
fun AdminDashboardScreen(
    orders: List<OrderEntity>,
    restaurants: List<RestaurantEntity>,
    onSwitchMode: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = "Platform analytics, dietary verification audits and ecosystem health",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Platform Key Metrics
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
                        MetricCard(title = "Gross Orders", value = "$${"%.2f".format(orders.sumOf { it.totalAmount } + 12840.0)}", modifier = Modifier.weight(1f))
                        MetricCard(title = "Total Orders", value = "${orders.size + 842}", modifier = Modifier.weight(1f))
                        MetricCard(title = "Dietary Verified", value = "${restaurants.size} / ${restaurants.size}", modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Dietary Safety Compliance Log
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
