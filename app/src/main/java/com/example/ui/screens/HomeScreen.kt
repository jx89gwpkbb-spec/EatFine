package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DietaryFilterState
import com.example.data.model.DietaryRestriction
import com.example.data.model.OrderEntity
import com.example.data.model.RestaurantEntity
import com.example.ui.components.DietaryBadge
import com.example.ui.components.DietaryFilterPillsRow
import com.example.ui.components.RealTimeOrderTrackingCard
import com.example.ui.components.RestaurantCard
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.FreshGreen

@Composable
fun HomeScreen(
    restaurants: List<RestaurantEntity>,
    favoriteIds: Set<String>,
    filterState: DietaryFilterState,
    onToggleDietaryRestriction: (DietaryRestriction) -> Unit,
    pureVegOnly: Boolean,
    onTogglePureVeg: () -> Unit,
    onOpenFilterSheet: () -> Unit,
    onResetFilters: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectRestaurant: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onNavigateExplore: () -> Unit,
    activeOngoingOrder: OrderEntity? = null,
    onTrackOrder: (String) -> Unit = {},
    onAdvanceOrderStatus: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Hero Tagline & Search Header
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EatFine",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandOrange
                        )
                        Text(
                            text = "Good Food. Great Moments.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FreshGreen.copy(alpha = 0.12f),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🥗", fontSize = 12.sp)
                            Text(
                                text = "Dietary Safe",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FreshGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar with Filter Action
                OutlinedTextField(
                    value = filterState.query,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search pizza, vegan bowls, halal biryani...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = BrandOrange
                        )
                    },
                    trailingIcon = {
                        if (filterState.query.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        } else {
                            IconButton(onClick = onOpenFilterSheet) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Tune filters",
                                    tint = BrandOrange
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )
            }
        }

        // Real-Time Order Tracking Component for Ongoing Orders
        if (activeOngoingOrder != null) {
            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    RealTimeOrderTrackingCard(
                        order = activeOngoingOrder,
                        onTrackClick = onTrackOrder,
                        onAdvanceStatus = onAdvanceOrderStatus
                    )
                }
            }
        }

        // Dietary Restriction Filter Pills Row (Core Feature requested!)
        item {
            val activeFilterCount = (if (pureVegOnly) 1 else 0) + filterState.selectedRestrictions.size
            DietaryFilterPillsRow(
                selectedRestrictions = filterState.selectedRestrictions,
                onToggleRestriction = onToggleDietaryRestriction,
                pureVegOnly = pureVegOnly,
                onTogglePureVeg = onTogglePureVeg,
                onOpenFilterSheet = onOpenFilterSheet,
                activeFilterCount = activeFilterCount
            )
        }

        // Active Dietary Filter Notice Banner
        if (filterState.selectedRestrictions.isNotEmpty() || pureVegOnly) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandOrange.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandOrange.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Filtered info",
                                tint = BrandOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = buildString {
                                    append("Filtered by: ")
                                    if (pureVegOnly) append("🌱 Pure Veg  ")
                                    append(filterState.selectedRestrictions.joinToString(" • ") { "${it.iconEmoji} ${it.shortTag}" })
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandOrange
                            )
                        }

                        IconButton(
                            onClick = onResetFilters,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear dietary filters",
                                tint = BrandOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Hero Food Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateExplore() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.eatfine_hero_banner_1790732425934),
                    contentDescription = "EatFine Special Feast",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(16.dp)
                ) {
                    Surface(
                        color = BrandOrange,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "50% OFF FIRST ORDER",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Craving Good Food?",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Use code EATFINE50 • Dietary certified",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Quick Category Row
        item {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Text(
                    text = "WHAT'S ON YOUR MIND?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                val categories = listOf(
                    Triple("Healthy", "🥗", "Vegan & Bowls"),
                    Triple("Pizza", "🍕", "Stone Baked"),
                    Triple("Biryani", "🍲", "Halal Dum"),
                    Triple("Thali", "🍱", "Pure Veg & Jain"),
                    Triple("Bakery", "🥐", "Gluten-Free"),
                    Triple("Mediterranean", "🫒", "Mezze & Grill")
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { (cat, emoji, subtitle) ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .width(100.dp)
                                .clickable { onSearchQueryChange(cat) }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = emoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cat,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = subtitle,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Restaurants List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (filterState.selectedRestrictions.isNotEmpty() || pureVegOnly)
                            "Dietary Filtered Kitchens (${restaurants.size})"
                        else "Restaurants Near You (${restaurants.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Prepared with strict allergen & diet standards",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (filterState.isActive) {
                    Button(
                        onClick = onResetFilters,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(text = "Reset", color = BrandOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Empty state if no restaurants match dietary restrictions
        if (restaurants.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🥦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching dietary kitchens found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try unchecking some dietary filters or switching between 'Match ALL' and 'Match ANY' restrictions.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onResetFilters,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                        ) {
                            Text("Reset Dietary Filters", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Restaurant Items
            items(restaurants, key = { it.id }) { restaurant ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    RestaurantCard(
                        restaurant = restaurant,
                        isFavorite = restaurant.id in favoriteIds,
                        onToggleFavorite = { onToggleFavorite(restaurant.id) },
                        onClick = { onSelectRestaurant(restaurant.id) }
                    )
                }
            }
        }
    }
}
