package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItemEntity
import com.example.data.model.MenuItemEntity
import com.example.data.model.RestaurantEntity
import com.example.data.model.ReviewEntity
import com.example.ui.components.DietaryBadge
import com.example.ui.components.MenuItemCard
import com.example.ui.components.RestaurantReviewsSummaryCard
import com.example.ui.components.ReviewCardItem
import com.example.ui.components.WriteReviewBottomSheet
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.FreshGreen

@Composable
fun RestaurantDetailScreen(
    restaurant: RestaurantEntity,
    menuItems: List<MenuItemEntity>,
    cartItems: List<CartItemEntity>,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToCart: (MenuItemEntity) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onBookTable: () -> Unit,
    reviews: List<ReviewEntity> = emptyList(),
    onSubmitReview: (Float, String, String, String) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedDetailTab by remember { mutableIntStateOf(0) } // 0: Menu, 1: Reviews & Ratings
    var isWriteReviewOpen by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = remember(menuItems) {
        listOf("All") + menuItems.map { it.category }.distinct()
    }

    val filteredItems = remember(menuItems, selectedCategory) {
        if (selectedCategory == "All") menuItems else menuItems.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("restaurant_detail_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Cover Photo & Header Actions
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Image(
                    painter = painterResource(id = restaurant.bannerDrawableRes),
                    contentDescription = restaurant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                // Top Back and Favorite Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("detail_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFFFF4D4F) else Color.White
                        )
                    }
                }

                // Bottom badges on photo
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (restaurant.isPureVeg) {
                        Surface(
                            color = FreshGreen,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🌱 100% PURE VEG",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (restaurant.offerText.isNotBlank()) {
                        Surface(
                            color = BrandOrange,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = restaurant.offerText,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // Restaurant Info Block
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = restaurant.tagline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = restaurant.cuisines,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = FreshGreen,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { selectedDetailTab = 1 }
                            .testTag("detail_rating_badge")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "%.1f".format(restaurant.rating),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "${restaurant.reviewCount} reviews",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Delivery, Distance & Address
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = "Time",
                            tint = BrandOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${restaurant.deliveryTimeMin} mins",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Distance",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${restaurant.distanceKm} km away",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = if (restaurant.deliveryFee == 0.0) "Free Delivery" else "$${restaurant.deliveryFee} Delivery",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (restaurant.deliveryFee == 0.0) FreshGreen else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dietary Safety & Certification Box
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FreshGreen.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FreshGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
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
                                text = "DIETARY VERIFIED KITCHEN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = FreshGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = restaurant.certificationNote,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Dietary Badges Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            restaurant.dietaryList.forEach { restriction ->
                                DietaryBadge(restriction = restriction)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Table Reservation Button
                OutlinedButton(
                    onClick = onBookTable,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_table_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.EventSeat,
                        contentDescription = "Reservation",
                        tint = BrandOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Book a Table (With Dietary Preferences)",
                        color = BrandOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Tab Selector: Menu | Reviews & Ratings
        item {
            TabRow(
                selectedTabIndex = selectedDetailTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BrandOrange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Tab(
                    selected = selectedDetailTab == 0,
                    onClick = { selectedDetailTab = 0 },
                    text = {
                        Text(
                            text = "Menu (${menuItems.size})",
                            fontWeight = if (selectedDetailTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("tab_menu")
                )
                Tab(
                    selected = selectedDetailTab == 1,
                    onClick = { selectedDetailTab = 1 },
                    text = {
                        Text(
                            text = "Reviews (${if (reviews.isNotEmpty()) reviews.size else restaurant.reviewCount})",
                            fontWeight = if (selectedDetailTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("tab_reviews")
                )
            }
        }

        if (selectedDetailTab == 0) {
            // TAB 0: MENU
            // Category Filter Chips
            item {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "MENU CATEGORIES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandOrange,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Menu Items List
            items(filteredItems, key = { it.id }) { item ->
                val cartEntry = cartItems.find { it.menuItemId == item.id }
                val quantity = cartEntry?.quantity ?: 0

                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    MenuItemCard(
                        item = item,
                        cartQuantity = quantity,
                        onAddToCart = { onAddToCart(item) },
                        onIncreaseQuantity = {
                            cartEntry?.let { onUpdateQuantity(it.id, quantity + 1) }
                        },
                        onDecreaseQuantity = {
                            cartEntry?.let { onUpdateQuantity(it.id, quantity - 1) }
                        }
                    )
                }
            }

            // Bottom Review Teaser on Menu tab
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "%.1f".format(restaurant.rating),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "★", color = Color(0xFFFFB020), fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${if (reviews.isNotEmpty()) reviews.size else restaurant.reviewCount} customer reviews)",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Dietary safety & quality feedback",
                                fontSize = 12.sp,
                                color = FreshGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { selectedDetailTab = 1 },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                        ) {
                            Text("See Reviews", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // TAB 1: REVIEWS & RATINGS
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    RestaurantReviewsSummaryCard(
                        restaurant = restaurant,
                        reviews = reviews,
                        onWriteReviewClick = { isWriteReviewOpen = true }
                    )
                }
            }

            if (reviews.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "✍️", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No customer reviews yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Be the first verified foodie to share your dietary and taste experience!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                        Button(
                            onClick = { isWriteReviewOpen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                        ) {
                            Text("Write First Review")
                        }
                    }
                }
            } else {
                items(reviews, key = { it.id }) { review ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        ReviewCardItem(review = review)
                    }
                }
            }
        }
    }

    // Modal Sheet for writing a review
    if (isWriteReviewOpen) {
        WriteReviewBottomSheet(
            restaurant = restaurant,
            onDismiss = { isWriteReviewOpen = false },
            onSubmit = { rating, comment, dietaryTags, userName ->
                onSubmitReview(rating, comment, dietaryTags, userName)
                isWriteReviewOpen = false
            }
        )
    }
}
