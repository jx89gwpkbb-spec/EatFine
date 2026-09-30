package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppMode
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.BrandOrangeLight
import com.example.ui.viewmodel.CustomerTab

@Composable
fun EatFineTopBar(
    address: String,
    currentMode: AppMode,
    onSelectMode: (AppMode) -> Unit,
    activeDietaryCount: Int,
    onOpenDietaryFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showModeDialog by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location and Brand
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { /* address picker */ }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = BrandOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "DELIVERING TO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand location",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Role Switcher pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when (currentMode) {
                    AppMode.CUSTOMER -> BrandOrange.copy(alpha = 0.12f)
                    AppMode.RESTAURANT_PARTNER -> Color(0xFF1971C2).copy(alpha = 0.12f)
                    AppMode.DELIVERY_PARTNER -> Color(0xFF2B8A3E).copy(alpha = 0.12f)
                    AppMode.ADMIN -> Color(0xFF7048E8).copy(alpha = 0.12f)
                },
                modifier = Modifier
                    .clickable { showModeDialog = true }
                    .testTag("app_mode_switcher_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val (icon, title) = when (currentMode) {
                        AppMode.CUSTOMER -> Icons.Default.Restaurant to "Customer"
                        AppMode.RESTAURANT_PARTNER -> Icons.Default.Storefront to "Partner"
                        AppMode.DELIVERY_PARTNER -> Icons.Default.DeliveryDining to "Driver"
                        AppMode.ADMIN -> Icons.Default.AdminPanelSettings to "Admin"
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(16.dp),
                        tint = when (currentMode) {
                            AppMode.CUSTOMER -> BrandOrange
                            AppMode.RESTAURANT_PARTNER -> Color(0xFF1971C2)
                            AppMode.DELIVERY_PARTNER -> Color(0xFF2B8A3E)
                            AppMode.ADMIN -> Color(0xFF7048E8)
                        }
                    )
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (currentMode) {
                            AppMode.CUSTOMER -> BrandOrange
                            AppMode.RESTAURANT_PARTNER -> Color(0xFF1971C2)
                            AppMode.DELIVERY_PARTNER -> Color(0xFF2B8A3E)
                            AppMode.ADMIN -> Color(0xFF7048E8)
                        }
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Switch",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }

    if (showModeDialog) {
        AlertDialog(
            onDismissRequest = { showModeDialog = false },
            title = {
                Text(text = "Switch Platform View", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "EatFine connects customers, restaurants, drivers and administrators in one ecosystem:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    AppModeOption(
                        mode = AppMode.CUSTOMER,
                        label = "Customer App",
                        desc = "Order food, filter dietary tags, book tables",
                        isSelected = currentMode == AppMode.CUSTOMER,
                        onSelect = { onSelectMode(AppMode.CUSTOMER); showModeDialog = false }
                    )
                    AppModeOption(
                        mode = AppMode.RESTAURANT_PARTNER,
                        label = "Restaurant Partner Dashboard",
                        desc = "Accept orders, kitchen prep, toggle items",
                        isSelected = currentMode == AppMode.RESTAURANT_PARTNER,
                        onSelect = { onSelectMode(AppMode.RESTAURANT_PARTNER); showModeDialog = false }
                    )
                    AppModeOption(
                        mode = AppMode.DELIVERY_PARTNER,
                        label = "Delivery Partner App",
                        desc = "Accept delivery, route map, proof of drop-off",
                        isSelected = currentMode == AppMode.DELIVERY_PARTNER,
                        onSelect = { onSelectMode(AppMode.DELIVERY_PARTNER); showModeDialog = false }
                    )
                    AppModeOption(
                        mode = AppMode.ADMIN,
                        label = "Admin Control Hub",
                        desc = "Platform analytics, finances, restaurant verifications",
                        isSelected = currentMode == AppMode.ADMIN,
                        onSelect = { onSelectMode(AppMode.ADMIN); showModeDialog = false }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showModeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun AppModeOption(
    mode: AppMode,
    label: String,
    desc: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BrandOrange.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("select_mode_${mode.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onSelect)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun EatFineBottomNav(
    currentTab: CustomerTab,
    onTabSelected: (CustomerTab) -> Unit,
    activeOrdersCount: Int,
    favoritesCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("main_bottom_nav")
    ) {
        CustomerTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    when (tab) {
                        CustomerTab.HOME -> Icon(
                            if (isSelected) Icons.Default.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                        CustomerTab.EXPLORE -> Icon(
                            if (isSelected) Icons.Default.Explore else Icons.Outlined.Explore,
                            contentDescription = "Explore"
                        )
                        CustomerTab.ORDERS -> {
                            if (activeOrdersCount > 0) {
                                BadgedBox(badge = {
                                    Badge(containerColor = BrandOrange) {
                                        Text("$activeOrdersCount")
                                    }
                                }) {
                                    Icon(
                                        if (isSelected) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
                                        contentDescription = "Orders"
                                    )
                                }
                            } else {
                                Icon(
                                    if (isSelected) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
                                    contentDescription = "Orders"
                                )
                            }
                        }
                        CustomerTab.FAVORITES -> {
                            if (favoritesCount > 0) {
                                BadgedBox(badge = {
                                    Badge(
                                        containerColor = BrandOrange,
                                        contentColor = Color.White
                                    ) {
                                        Text("$favoritesCount")
                                    }
                                }) {
                                    Icon(
                                        if (isSelected) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Saved Favorites ($favoritesCount)"
                                    )
                                }
                            } else {
                                Icon(
                                    if (isSelected) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Saved Favorites"
                                )
                            }
                        }
                        CustomerTab.RESERVATIONS -> Icon(
                            Icons.Default.EventSeat,
                            contentDescription = "Tables"
                        )
                        CustomerTab.PROFILE -> Icon(
                            if (isSelected) Icons.Default.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    }
                },
                label = { Text(tab.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandOrange,
                    selectedTextColor = BrandOrange,
                    indicatorColor = BrandOrange.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
            )
        }
    }
}

@Composable
fun FloatingCartBar(
    itemCount: Int,
    subtotal: Double,
    onViewCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = itemCount > 0,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = BrandOrange,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { onViewCart() }
                .testTag("floating_cart_bar")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Cart",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "$itemCount ${if (itemCount == 1) "item" else "items"} added",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tap to checkout • Free dietary utensil packing",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${"%.2f".format(subtotal)}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VIEW CART →",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
