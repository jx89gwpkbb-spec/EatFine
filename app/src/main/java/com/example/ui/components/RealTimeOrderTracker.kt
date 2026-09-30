package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.WarmAmber
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Calculates progress float (0.0f to 1.0f) based on the order status lifecycle
 */
fun getOrderStatusProgress(status: OrderStatus): Float {
    return when (status) {
        OrderStatus.PLACED -> 0.15f
        OrderStatus.ACCEPTED -> 0.35f
        OrderStatus.PREPARING -> 0.60f
        OrderStatus.READY_FOR_PICKUP -> 0.78f
        OrderStatus.ON_THE_WAY -> 0.90f
        OrderStatus.DELIVERED -> 1.0f
        OrderStatus.CANCELLED -> 0.0f
    }
}

/**
 * Human-readable status subtitle message describing real-time action
 */
fun getOrderStatusDescription(status: OrderStatus, restaurantName: String, driverName: String): String {
    return when (status) {
        OrderStatus.PLACED -> "Order sent to $restaurantName. Awaiting confirmation."
        OrderStatus.ACCEPTED -> "$restaurantName accepted your order! Prepping kitchen station."
        OrderStatus.PREPARING -> "Chef is cooking your dietary-safe meal with fresh ingredients."
        OrderStatus.READY_FOR_PICKUP -> "Order sealed in insulated packaging. Driver arriving for pickup."
        OrderStatus.ON_THE_WAY -> "$driverName is on the way to your door with live GPS routing."
        OrderStatus.DELIVERED -> "Order successfully delivered! Bon appétit."
        OrderStatus.CANCELLED -> "This order was cancelled."
    }
}

/**
 * Real-Time Order Tracking Component
 *
 * Displays:
 * 1. Delivery status with pulsing real-time indicator
 * 2. Estimated arrival time (ETA in minutes and estimated clock time)
 * 3. Animated progress bar showing exact stage of ongoing order
 * 4. Milestone steps (Placed -> Kitchen -> Out for Delivery -> Delivered)
 * 5. Driver & dietary safety tags
 * 6. Interactive actions (navigate to live map / quick advance)
 */
@Composable
fun RealTimeOrderTrackingCard(
    order: OrderEntity,
    onTrackClick: (String) -> Unit,
    onAdvanceStatus: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Current time simulation tick for real-time elapsed calculations
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // update every 10 seconds
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    // Calculate dynamic ETA remaining
    val elapsedMinutes = ((currentTimeMillis - order.placedTimestamp) / (1000 * 60)).coerceAtLeast(0).toInt()
    val initialEstimated = order.estimatedDeliveryMinutes.coerceAtLeast(15)
    val remainingMinutes = when (order.status) {
        OrderStatus.DELIVERED -> 0
        OrderStatus.CANCELLED -> 0
        OrderStatus.ON_THE_WAY -> (initialEstimated - elapsedMinutes).coerceIn(4, 12)
        OrderStatus.READY_FOR_PICKUP -> (initialEstimated - elapsedMinutes).coerceIn(10, 16)
        OrderStatus.PREPARING -> (initialEstimated - elapsedMinutes).coerceIn(14, 25)
        OrderStatus.ACCEPTED -> (initialEstimated - elapsedMinutes).coerceIn(20, 30)
        OrderStatus.PLACED -> initialEstimated
    }

    // Estimated arrival clock time (e.g. 7:42 PM)
    val arrivalTimeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val arrivalClockTime = remember(currentTimeMillis, remainingMinutes) {
        val arrivalTime = Date(currentTimeMillis + (remainingMinutes * 60 * 1000L))
        arrivalTimeFormat.format(arrivalTime)
    }

    // Animated Progress Bar value
    val targetProgress = getOrderStatusProgress(order.status)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "orderProgressBar"
    )

    // Pulsing indicator for active stages
    val infiniteTransition = rememberInfiniteTransition(label = "pulseGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val isOngoing = order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(
            1.2.dp,
            if (isOngoing) BrandOrange.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTrackClick(order.orderId) }
            .testTag("realtime_order_tracking_component")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Live Indicator, Restaurant Name, Order ID, and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Pulsing live radar dot
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = if (isOngoing) BrandOrange.copy(alpha = pulseAlpha) else FreshGreen,
                                shape = CircleShape
                            )
                    )

                    Column {
                        Text(
                            text = order.restaurantName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Order #${order.orderId} • ${order.deliveryType.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Delivery Status Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (order.status) {
                        OrderStatus.DELIVERED -> FreshGreen.copy(alpha = 0.15f)
                        OrderStatus.CANCELLED -> Color(0xFFD32F2F).copy(alpha = 0.15f)
                        OrderStatus.ON_THE_WAY -> BrandOrange
                        else -> BrandOrange.copy(alpha = 0.15f)
                    },
                    contentColor = when (order.status) {
                        OrderStatus.DELIVERED -> FreshGreen
                        OrderStatus.CANCELLED -> Color(0xFFD32F2F)
                        OrderStatus.ON_THE_WAY -> Color.White
                        else -> BrandOrange
                    },
                    modifier = Modifier.testTag("status_badge_${order.status.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (order.status == OrderStatus.ON_THE_WAY) {
                            Icon(
                                imageVector = Icons.Default.DeliveryDining,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = order.status.display.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Estimated Arrival Time Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isOngoing) BrandOrange.copy(alpha = 0.08f) else FreshGreen.copy(alpha = 0.08f),
                border = BorderStroke(
                    1.dp,
                    if (isOngoing) BrandOrange.copy(alpha = 0.25f) else FreshGreen.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (isOngoing) BrandOrange else FreshGreen,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isOngoing) Icons.Default.Schedule else Icons.Default.Check,
                                contentDescription = "ETA Clock",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (order.status == OrderStatus.DELIVERED) "DELIVERED AT DOORSTEP"
                                else "ESTIMATED ARRIVAL TIME",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOngoing) BrandOrange else FreshGreen
                            )
                            Text(
                                text = if (order.status == OrderStatus.DELIVERED) "Completed successfully"
                                else "$remainingMinutes mins (${arrivalClockTime})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Progress percentage badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOngoing) BrandOrange else FreshGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar with Animated Gradient / Track
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .testTag("realtime_order_progress_bar"),
                    color = if (order.status == OrderStatus.DELIVERED) FreshGreen else BrandOrange,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Milestone step indicators below progress bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    val milestones = listOf(
                        Triple(OrderStatus.PLACED, "Placed", Icons.Default.Receipt),
                        Triple(OrderStatus.PREPARING, "Kitchen", Icons.Default.SoupKitchen),
                        Triple(OrderStatus.ON_THE_WAY, "On The Way", Icons.Default.DeliveryDining),
                        Triple(OrderStatus.DELIVERED, "Delivered", Icons.Default.Check)
                    )

                    milestones.forEach { (milestoneStatus, label, icon) ->
                        val isReached = order.status.stepIndex >= milestoneStatus.stepIndex && order.status != OrderStatus.CANCELLED
                        val isCurrent = order.status == milestoneStatus

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(72.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        when {
                                            isReached && order.status == OrderStatus.DELIVERED -> FreshGreen
                                            isReached -> BrandOrange
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isReached) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) BrandOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Detailed Status Message
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = BrandOrange,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = getOrderStatusDescription(order.status, order.restaurantName, order.driverName),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }

            // Chef Dietary Safeguard notice (if applicable)
            if (order.chefDietaryInstructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FreshGreen.copy(alpha = 0.1f),
                    border = BorderStroke(0.8.dp, FreshGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Dietary",
                            tint = FreshGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Dietary Safeguard: ${order.chefDietaryInstructions}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FreshGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Track Live on Map + optional status stepper simulation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onTrackClick(order.orderId) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("track_live_map_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Track",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Track Live on Map →",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (onAdvanceStatus != null && isOngoing) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BrandOrange.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, BrandOrange.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .height(42.dp)
                            .clickable { onAdvanceStatus(order.orderId) }
                            .testTag("quick_advance_status_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Advance step",
                                tint = BrandOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Next Step",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandOrange
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Floating Real-Time Tracker Bar
 * Useful for displaying at bottom/top while user is browsing other screens
 */
@Composable
fun RealTimeOrderTrackerCompactBar(
    order: OrderEntity,
    onTrackClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = getOrderStatusProgress(order.status)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "compactBarProgress"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E293B), // Dark slate
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTrackClick(order.orderId) }
            .testTag("compact_order_tracker_bar")
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(BrandOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "${order.status.display} • ${order.restaurantName}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Estimated Arrival: ${order.estimatedDeliveryMinutes} mins",
                            color = Color(0xFFFFD43B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TRACK",
                        color = BrandOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Track",
                        tint = BrandOrange,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mini progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = BrandOrange,
                trackColor = Color(0xFF334155),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
