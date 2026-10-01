package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RestaurantEntity
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.FreshGreen
import kotlin.math.roundToInt

fun launchGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String) {
    val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        setPackage("com.google.android.apps.maps")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(mapIntent)
    } catch (_: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(webIntent)
        } catch (_: Exception) {}
    }
}

@Composable
fun RestaurantMapView(
    restaurants: List<RestaurantEntity>,
    onSelectRestaurant: (String) -> Unit,
    onSwitchToListView: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onSwitchToListView != null) {
        BackHandler {
            onSwitchToListView()
        }
    }

    val context = LocalContext.current
    var selectedRestaurantId by remember { mutableStateOf<String?>(restaurants.firstOrNull()?.id) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    var selectedCuisineFilter by remember { mutableStateOf("All") }
    var maxDistanceFilterKm by remember { mutableFloatStateOf(5.0f) }

    val cuisineCategories = listOf("All", "Healthy", "Pizza", "Biryani", "Keto", "Mediterranean", "Bakery")

    val filteredRestaurants = remember(restaurants, selectedCuisineFilter, maxDistanceFilterKm) {
        restaurants.filter { r ->
            val matchesCuisine = if (selectedCuisineFilter == "All") true else {
                r.cuisines.contains(selectedCuisineFilter, ignoreCase = true) ||
                r.heroCategory.contains(selectedCuisineFilter, ignoreCase = true)
            }
            val matchesDistance = r.distanceKm <= maxDistanceFilterKm
            matchesCuisine && matchesDistance
        }
    }

    val selectedRestaurant = remember(selectedRestaurantId, restaurants) {
        restaurants.find { it.id == selectedRestaurantId }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8ECEF))
            .testTag("restaurant_map_view")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val centerX = widthPx / 2f
        val centerY = heightPx / 2f

        // Interactive Map Canvas: Roads, Blocks, Parks, Waterways
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffsetX += dragAmount.x
                        panOffsetY += dragAmount.y
                    }
                }
        ) {
            // Background land
            drawRect(Color(0xFFF1F3F4))

            // Water feature (Bay/River on the east)
            val waterPath = Path().apply {
                moveTo(widthPx * 0.85f + panOffsetX * 0.3f, 0f)
                cubicTo(
                    widthPx * 0.80f + panOffsetX * 0.3f, heightPx * 0.35f,
                    widthPx * 0.90f + panOffsetX * 0.3f, heightPx * 0.65f,
                    widthPx * 0.82f + panOffsetX * 0.3f, heightPx
                )
                lineTo(widthPx, heightPx)
                lineTo(widthPx, 0f)
                close()
            }
            drawPath(waterPath, color = Color(0xFFA5C9EB))

            // Green Parks & Gardens
            drawRoundRect(
                color = Color(0xFFCCE8CF),
                topLeft = Offset(widthPx * 0.12f + panOffsetX * 0.8f, heightPx * 0.18f + panOffsetY * 0.8f),
                size = Size(widthPx * 0.28f * zoomScale, heightPx * 0.16f * zoomScale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
            )

            drawRoundRect(
                color = Color(0xFFCCE8CF),
                topLeft = Offset(widthPx * 0.52f + panOffsetX * 0.8f, heightPx * 0.62f + panOffsetY * 0.8f),
                size = Size(widthPx * 0.22f * zoomScale, heightPx * 0.14f * zoomScale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
            )

            // Major Roads & Avenues Grid
            val roadColor = Color(0xFFFFFFFF)
            val roadBorder = Color(0xFFD6DBDF)

            // Horizontal Avenues
            for (i in 1..7) {
                val y = (heightPx * (i / 8f) + panOffsetY * 0.9f)
                drawLine(
                    color = roadBorder,
                    start = Offset(0f, y),
                    end = Offset(widthPx, y),
                    strokeWidth = 14f * zoomScale
                )
                drawLine(
                    color = roadColor,
                    start = Offset(0f, y),
                    end = Offset(widthPx, y),
                    strokeWidth = 10f * zoomScale
                )
            }

            // Vertical Boulevards
            for (i in 1..6) {
                val x = (widthPx * (i / 7f) + panOffsetX * 0.9f)
                drawLine(
                    color = roadBorder,
                    start = Offset(x, 0f),
                    end = Offset(x, heightPx),
                    strokeWidth = 14f * zoomScale
                )
                drawLine(
                    color = roadColor,
                    start = Offset(x, 0f),
                    end = Offset(x, heightPx),
                    strokeWidth = 10f * zoomScale
                )
            }

            // Diagonal Highway
            drawLine(
                color = Color(0xFFFFE082),
                start = Offset(0f, heightPx * 0.8f + panOffsetY * 0.9f),
                end = Offset(widthPx * 0.85f, 0f + panOffsetY * 0.9f),
                strokeWidth = 8f * zoomScale
            )

            // User Location Pulse Indicator ("Delivering to you")
            val userX = centerX + panOffsetX
            val userY = centerY + 40f + panOffsetY
            drawCircle(
                color = BrandOrange.copy(alpha = 0.2f),
                radius = 32f * zoomScale,
                center = Offset(userX, userY)
            )
            drawCircle(
                color = Color.White,
                radius = 12f * zoomScale,
                center = Offset(userX, userY)
            )
            drawCircle(
                color = BrandOrange,
                radius = 8f * zoomScale,
                center = Offset(userX, userY)
            )
        }

        // Restaurant Pins placed on the Map Canvas
        filteredRestaurants.forEachIndexed { index, rest ->
            // Distribute restaurants deterministically around center
            val angle = (index * (360.0 / restaurants.size.coerceAtLeast(1))) * (Math.PI / 180.0)
            val radius = (120f + (index % 3) * 75f) * zoomScale
            val pinX = (centerX + Math.cos(angle).toFloat() * radius + panOffsetX).roundToInt()
            val pinY = (centerY + Math.sin(angle).toFloat() * radius + panOffsetY).roundToInt()

            val isSelected = rest.id == selectedRestaurantId

            Box(
                modifier = Modifier
                    .offset { IntOffset(pinX - 40, pinY - 50) }
                    .clickable { selectedRestaurantId = rest.id }
                    .testTag("map_marker_${rest.id}")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(90.dp)
                ) {
                    // Rating Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) BrandOrange else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shadowElevation = if (isSelected) 8.dp else 3.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = when (rest.heroCategory) {
                                    "Healthy" -> "🥗"
                                    "Pizza" -> "🍕"
                                    "Biryani" -> "🍛"
                                    "Bakery" -> "🥖"
                                    "Mediterranean" -> "🫒"
                                    else -> "🍽️"
                                },
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${rest.rating}★",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Marker Pin Body
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 36.dp else 28.dp)
                            .shadow(6.dp, CircleShape)
                            .background(
                                color = if (isSelected) BrandOrange else Color(0xFF1E293B),
                                shape = CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (rest.isPureVeg) "🌱" else "🍽️",
                            fontSize = if (isSelected) 14.sp else 11.sp
                        )
                    }

                    // Short Name label
                    Text(
                        text = rest.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 3.dp)
                    )
                }
            }
        }

        // Top Filter Bar on Map
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = BrandOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Interactive Dining Map",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandOrange.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${filteredRestaurants.size} nearby",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (onSwitchToListView != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clickable { onSwitchToListView() }
                                        .testTag("map_to_list_toggle")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ViewList,
                                            contentDescription = "Switch to List View",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "List",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Cuisine filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cuisineCategories.forEach { category ->
                            val isSelected = selectedCuisineFilter == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCuisineFilter = category },
                                label = { Text(category, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Zoom & Recenter Controls (Right Side)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                    zoomScale = 1.0f
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BrandOrange,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    modifier = Modifier.size(20.dp)
                )
            }

            FloatingActionButton(
                onClick = { zoomScale = (zoomScale + 0.25f).coerceAtMost(2.0f) },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
            }

            FloatingActionButton(
                onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.6f) },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
            }
        }

        // Bottom Selected Restaurant Preview Card
        AnimatedVisibility(
            visible = selectedRestaurant != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            selectedRestaurant?.let { rest ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("map_selected_card_${rest.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = rest.bannerDrawableRes),
                                    contentDescription = rest.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = rest.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (rest.isPureVeg) {
                                            Text(text = "🌱", fontSize = 12.sp)
                                        }
                                    }

                                    Text(
                                        text = rest.cuisines,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = FreshGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = FreshGreen,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Text(
                                                    text = "${rest.rating}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FreshGreen
                                                )
                                            }
                                        }

                                        Text(
                                            text = "• ${rest.distanceKm} km away",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Text(
                                            text = "• ${rest.deliveryTimeMin} mins",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = BrandOrange
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { selectedRestaurantId = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Open in Google Maps vs Order Now
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    launchGoogleMaps(
                                        context = context,
                                        latitude = rest.latitude,
                                        longitude = rest.longitude,
                                        label = rest.name
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BrandOrange),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("google_maps_directions_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Directions,
                                    contentDescription = null,
                                    tint = BrandOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Google Maps",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandOrange
                                )
                            }

                            Button(
                                onClick = { onSelectRestaurant(rest.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("map_view_menu_button")
                            ) {
                                Text(
                                    text = "View Menu →",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
