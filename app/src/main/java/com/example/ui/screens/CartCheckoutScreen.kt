package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItemEntity
import com.example.data.model.DeliveryType
import com.example.data.model.OrderEntity
import com.example.data.model.RestaurantEntity
import com.example.ui.components.VegNonVegSymbol
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.FreshGreen

@Composable
fun CartCheckoutScreen(
    cartItems: List<CartItemEntity>,
    restaurant: RestaurantEntity?,
    deliveryAddress: String,
    onAddressChange: (String) -> Unit,
    deliveryType: DeliveryType,
    onDeliveryTypeChange: (DeliveryType) -> Unit,
    activeCoupon: String?,
    onApplyCoupon: (String) -> Unit,
    chefDietaryNotes: String,
    onChefNotesChange: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onClearCart: () -> Unit,
    onPlaceOrder: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var couponInput by remember { mutableStateOf(activeCoupon ?: "") }
    var selectedTip by remember { mutableStateOf(2.0) }

    val subtotal = remember(cartItems) { cartItems.sumOf { it.price * it.quantity } }
    val deliveryFee = remember(restaurant, deliveryType, activeCoupon) {
        if (deliveryType == DeliveryType.PICKUP || activeCoupon?.uppercase() == "FREESHIP") 0.0
        else restaurant?.deliveryFee ?: 1.99
    }
    val discount = remember(subtotal, activeCoupon) {
        when (activeCoupon?.uppercase()) {
            "EATFINE50" -> (subtotal * 0.50).coerceAtMost(10.0)
            "HEALTHY20" -> (subtotal * 0.20).coerceAtMost(8.0)
            else -> 0.0
        }
    }
    val taxesAndFees = remember(subtotal) { (subtotal * 0.08) + 1.25 }
    val total = remember(subtotal, discount, deliveryFee, taxesAndFees, selectedTip) {
        (subtotal - discount + deliveryFee + taxesAndFees + selectedTip).coerceAtLeast(0.0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("cart_checkout_screen")
    ) {
        // Top App Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Column {
                        Text(
                            text = "Your Cart & Checkout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = restaurant?.name ?: "EatFine Order",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = onClearCart) {
                        Text("Clear", color = Color(0xFFD32F2F))
                    }
                }
            }
        }

        if (cartItems.isEmpty()) {
            // Empty Cart State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🛒", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your cart is waiting for something delicious",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Discover healthy bowls, artisan pizzas, or pure veg feasts customized to your dietary needs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Explore Restaurants", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Delivery vs Pickup Selector
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (deliveryType == DeliveryType.DELIVERY) BrandOrange else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onDeliveryTypeChange(DeliveryType.DELIVERY) }
                            ) {
                                Text(
                                    text = "🛵 Delivery",
                                    color = if (deliveryType == DeliveryType.DELIVERY) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (deliveryType == DeliveryType.PICKUP) BrandOrange else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onDeliveryTypeChange(DeliveryType.PICKUP) }
                            ) {
                                Text(
                                    text = "🛍️ Self Pickup",
                                    color = if (deliveryType == DeliveryType.PICKUP) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Delivery Address Card
                if (deliveryType == DeliveryType.DELIVERY) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Address",
                                        tint = BrandOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Delivery Address",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = deliveryAddress,
                                    onValueChange = onAddressChange,
                                    singleLine = false,
                                    maxLines = 2,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Dietary Preparation Instructions for the Chef
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = FreshGreen.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, FreshGreen.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Dietary safety",
                                    tint = FreshGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Chef Dietary & Allergen Instructions",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = FreshGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = chefDietaryNotes,
                                onValueChange = onChefNotesChange,
                                placeholder = {
                                    Text(
                                        text = "e.g. Severe peanut allergy, separate clean prep utensils, extra gluten-free wrap...",
                                        fontSize = 12.sp
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chef_dietary_notes_input")
                            )
                        }
                    }
                }

                // Cart Items List
                item {
                    Text(
                        text = "ORDER ITEMS (${cartItems.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandOrange
                    )
                }

                items(cartItems, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    VegNonVegSymbol(isVeg = item.isVeg)
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$${"%.2f".format(item.price)} each",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Steppers
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandOrange.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = { onUpdateQuantity(item.id, item.quantity - 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (item.quantity == 1) Icons.Default.DeleteOutline else Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            tint = BrandOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = "${item.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BrandOrange,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = { onUpdateQuantity(item.id, item.quantity + 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase",
                                            tint = BrandOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Coupon Code Section
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Discount,
                                    contentDescription = "Promo",
                                    tint = BrandOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Offers & Promo Code",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = couponInput,
                                    onValueChange = { couponInput = it },
                                    placeholder = { Text("Try EATFINE50 or HEALTHY20", fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { onApplyCoupon(couponInput) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(52.dp)
                                ) {
                                    Text("Apply")
                                }
                            }
                            if (activeCoupon != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "✓ Coupon '$activeCoupon' applied successfully!",
                                    fontSize = 12.sp,
                                    color = FreshGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Tip Section
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Say thanks with a Tip for your Delivery Partner",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1.0, 2.0, 3.0, 5.0).forEach { tipAmount ->
                                    val isSelected = selectedTip == tipAmount
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) BrandOrange else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedTip = tipAmount }
                                    ) {
                                        Text(
                                            text = "$${tipAmount.toInt()}",
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bill Summary
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "BILL BREAKDOWN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            BillRow("Item Subtotal", "$${"%.2f".format(subtotal)}")
                            if (discount > 0) {
                                BillRow("EatFine Promo Discount", "-$${"%.2f".format(discount)}", isHighlight = true)
                            }
                            BillRow(
                                "Delivery Fee",
                                if (deliveryFee == 0.0) "FREE" else "$${"%.2f".format(deliveryFee)}"
                            )
                            BillRow("Taxes & Eco Packaging Fee", "$${"%.2f".format(taxesAndFees)}")
                            if (selectedTip > 0) {
                                BillRow("Driver Tip", "$${"%.2f".format(selectedTip)}")
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "To Pay",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "$${"%.2f".format(total)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandOrange
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Sticky Checkout Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = onPlaceOrder,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_place_order_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PLACE ORDER • $${"%.2f".format(total)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "EatFine Pay →",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BillRow(label: String, amount: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (isHighlight) FreshGreen else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = amount,
            fontSize = 13.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) FreshGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}
