package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.RealTimeOrderTrackingCard
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.FreshGreen

@Composable
fun OrderTrackingScreen(
    order: OrderEntity,
    onBack: () -> Unit,
    onAdvanceStatus: (String) -> Unit,
    onCancelOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "driverPulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("order_tracking_screen")
    ) {
        // App Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            text = "Live Order Tracking",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Order #${order.orderId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = when (order.status) {
                        OrderStatus.DELIVERED -> FreshGreen.copy(alpha = 0.15f)
                        OrderStatus.CANCELLED -> Color(0xFFD32F2F).copy(alpha = 0.15f)
                        else -> BrandOrange.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status.display,
                        color = when (order.status) {
                            OrderStatus.DELIVERED -> FreshGreen
                            OrderStatus.CANCELLED -> Color(0xFFD32F2F)
                            else -> BrandOrange
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Simulation Map Canvas
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Drawing map route lines
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val startX = 60.dp.toPx()
                            val startY = size.height * 0.7f
                            val endX = size.width - 60.dp.toPx()
                            val endY = size.height * 0.3f

                            // Road line
                            drawLine(
                                color = Color(0xFF334155),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 14f
                            )

                            // Animated route line
                            val currentFraction = when (order.status) {
                                OrderStatus.PLACED -> 0.1f
                                OrderStatus.ACCEPTED -> 0.25f
                                OrderStatus.PREPARING -> 0.4f
                                OrderStatus.READY_FOR_PICKUP -> 0.6f
                                OrderStatus.ON_THE_WAY -> (0.6f + 0.35f * pulseProgress)
                                OrderStatus.DELIVERED -> 1.0f
                                OrderStatus.CANCELLED -> 0.0f
                            }
                            val curX = startX + (endX - startX) * currentFraction
                            val curY = startY + (endY - startY) * currentFraction

                            drawLine(
                                color = Color(0xFFE8590C),
                                start = Offset(startX, startY),
                                end = Offset(curX, curY),
                                strokeWidth = 8f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                            )

                            // Driver dot
                            drawCircle(
                                color = Color(0xFFFF922B),
                                radius = 12f,
                                center = Offset(curX, curY)
                            )
                        }

                        // Restaurant Marker
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(color = Color(0xFF0F172A), shape = CircleShape) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "Restaurant",
                                    tint = FreshGreen,
                                    modifier = Modifier.padding(6.dp).size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = order.restaurantName,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Destination Marker
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Home",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = Color(0xFF0F172A), shape = CircleShape) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Destination",
                                    tint = BrandOrange,
                                    modifier = Modifier.padding(6.dp).size(18.dp)
                                )
                            }
                        }

                        // Live status pill at center
                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeliveryDining,
                                    contentDescription = "Delivery",
                                    tint = BrandOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (order.status == OrderStatus.DELIVERED) "Delivered!" else "ETA: ${order.estimatedDeliveryMinutes} mins",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Real-Time Delivery Status, ETA and Progress Bar Hero Component
            item {
                RealTimeOrderTrackingCard(
                    order = order,
                    onTrackClick = { /* already on tracking screen */ },
                    onAdvanceStatus = onAdvanceStatus
                )
            }

            // Status Stepper
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ORDER PROGRESS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandOrange
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val steps = listOf(
                            OrderStatus.PLACED to "Order placed & sent to restaurant",
                            OrderStatus.ACCEPTED to "Restaurant confirmed and accepted",
                            OrderStatus.PREPARING to "Chef is preparing your dietary meal",
                            OrderStatus.READY_FOR_PICKUP to "Food packed and waiting for delivery partner",
                            OrderStatus.ON_THE_WAY to "Alex is on the way to your door",
                            OrderStatus.DELIVERED to "Order successfully delivered! Enjoy your meal"
                        )

                        steps.forEachIndexed { index, (status, description) ->
                            val isCompleted = order.status.stepIndex >= status.stepIndex && order.status != OrderStatus.CANCELLED
                            val isCurrent = order.status == status

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            if (isCompleted) FreshGreen else MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = status.display,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isCurrent) BrandOrange else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = description,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (index < steps.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 11.dp)
                                        .width(2.dp)
                                        .height(18.dp)
                                        .background(if (isCompleted) FreshGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                )
                            }
                        }
                    }
                }
            }

            // Driver Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(BrandOrange.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeliveryDining,
                                    contentDescription = "Driver",
                                    tint = BrandOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = order.driverName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = order.driverVehicle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "★ 4.9 • 1,240 Deliveries",
                                    fontSize = 11.sp,
                                    color = FreshGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { /* call */ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(FreshGreen.copy(alpha = 0.12f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call driver",
                                    tint = FreshGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { /* chat */ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BrandOrange.copy(alpha = 0.12f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Chat with driver",
                                    tint = BrandOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Dietary Instructions Review
            if (order.chefDietaryInstructions.isNotBlank()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = FreshGreen.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FreshGreen.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Dietary note",
                                tint = FreshGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Chef Dietary Instructions Confirmed",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
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
            }

            // Order Summary & Receipt
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ORDER SUMMARY",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = order.itemsSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Delivered to: ${order.deliveryAddress}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Paid (${order.paymentMethod})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = "$${"%.2f".format(order.totalAmount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = BrandOrange
                            )
                        }
                    }
                }
            }

            // Testing / Simulation Action
            item {
                if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onAdvanceStatus(order.orderId) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("simulate_advance_order_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Next")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate Next Status Step (${order.status.display} → Next)")
                        }

                        OutlinedButton(
                            onClick = { onCancelOrder(order.orderId) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel Order", color = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }
    }
}
