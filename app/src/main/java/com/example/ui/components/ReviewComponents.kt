package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DietaryRestriction
import com.example.data.model.RestaurantEntity
import com.example.data.model.ReviewEntity
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.WarmAmber

/**
 * Interactive Star Rating Bar (1 to 5 stars)
 */
@Composable
fun InteractiveStarRatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 38.dp,
    maxStars: Int = 5,
    showLabel: Boolean = true
) {
    val ratingDescription = when {
        rating >= 5.0f -> "Exceptional! Perfect experience"
        rating >= 4.0f -> "Very Good! Exceeded expectations"
        rating >= 3.0f -> "Good / Satisfactory"
        rating >= 2.0f -> "Fair / Needs improvement"
        rating >= 1.0f -> "Disappointing"
        else -> "Tap to rate"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            for (i in 1..maxStars) {
                val isSelected = i <= rating
                val starScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "starScale$i"
                )
                val starTint by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFFFB020) else Color(0xFFCBD5E1),
                    animationSpec = tween(200),
                    label = "starTint$i"
                )

                IconButton(
                    onClick = { onRatingChange(i.toFloat()) },
                    modifier = Modifier
                        .size(starSize + 10.dp)
                        .scale(starScale)
                        .testTag("star_button_$i")
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Rate $i stars",
                        tint = starTint,
                        modifier = Modifier.size(starSize)
                    )
                }
            }
        }

        if (showLabel) {
            Text(
                text = ratingDescription,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (rating > 0) BrandOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * Display-only Star Rating (Read only)
 */
@Composable
fun StarRatingDisplay(
    rating: Float,
    modifier: Modifier = Modifier,
    starSize: Dp = 14.dp,
    color: Color = Color(0xFFFFB020)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
    ) {
        for (i in 1..5) {
            val icon = when {
                i <= rating -> Icons.Default.Star
                i - 0.5f <= rating -> Icons.Default.StarHalf
                else -> Icons.Default.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (i <= rating || i - 0.5f <= rating) color else Color(0xFFCBD5E1),
                modifier = Modifier.size(starSize)
            )
        }
    }
}

/**
 * Restaurant Reviews Summary & Breakdown Card
 */
@Composable
fun RestaurantReviewsSummaryCard(
    restaurant: RestaurantEntity,
    reviews: List<ReviewEntity>,
    onWriteReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = if (reviews.isNotEmpty()) reviews.size else restaurant.reviewCount
    val avgRating = if (reviews.isNotEmpty()) {
        (Math.round((reviews.map { it.rating }.average()) * 10.0) / 10.0).toFloat()
    } else {
        restaurant.rating
    }

    // Distribution calculation
    val counts = IntArray(5) // 0: 1-star, 1: 2-star, ..., 4: 5-star
    if (reviews.isNotEmpty()) {
        reviews.forEach { r ->
            val idx = (r.rating.toInt() - 1).coerceIn(0, 4)
            counts[idx]++
        }
    } else {
        // default curve based on avg
        counts[4] = (totalCount * 0.75).toInt()
        counts[3] = (totalCount * 0.18).toInt()
        counts[2] = (totalCount * 0.05).toInt()
        counts[1] = (totalCount * 0.01).toInt()
        counts[0] = (totalCount * 0.01).toInt()
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("reviews_summary_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Big Score
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", avgRating),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StarRatingDisplay(rating = avgRating, starSize = 16.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalCount reviews",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Middle: Rating Breakdown bars (5 stars down to 1 star)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (star in 5 downTo 1) {
                        val count = counts[star - 1]
                        val fraction = if (totalCount > 0) count.toFloat() / totalCount.toFloat() else 0f

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "$star",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(12.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB020),
                                modifier = Modifier.size(10.dp)
                            )
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = BrandOrange,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Write Review Action Button
            Button(
                onClick = onWriteReviewClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("write_review_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RateReview,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rate & Write a Review",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Individual Review Card Item
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewCardItem(
    review: ReviewEntity,
    modifier: Modifier = Modifier
) {
    var helpfulCount by remember { mutableIntStateOf((review.id.hashCode() % 6).coerceAtLeast(0) + 1) }
    var isLiked by remember { mutableStateOf(false) }

    val avatarInitial = review.userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    val avatarGradient = when ((review.id.hashCode() % 3)) {
        0 -> Brush.linearGradient(listOf(BrandOrange, WarmAmber))
        1 -> Brush.linearGradient(listOf(FreshGreen, Color(0xFF20C997)))
        else -> Brush.linearGradient(listOf(Color(0xFF4C6EF5), Color(0xFF748FFC)))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("review_item_${review.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Avatar, Name, Verified Badge, Rating, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(avatarGradient, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = avatarInitial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = review.userName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Diner",
                                tint = FreshGreen,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StarRatingDisplay(rating = review.rating, starSize = 12.dp)
                            Text(
                                text = review.date,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Star badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFB020).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB020),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = String.format("%.1f", review.rating),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFD97706)
                        )
                    }
                }
            }

            // Dietary Tags Used (if any)
            if (review.dietaryTagsUsed.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    review.dietaryTagsUsed.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FreshGreen.copy(alpha = 0.12f),
                            border = BorderStroke(0.5.dp, FreshGreen.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = FreshGreen,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = tag,
                                    color = FreshGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Comment text
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            // Helpful footer
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isLiked) FreshGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clickable {
                        isLiked = !isLiked
                        helpfulCount += if (isLiked) 1 else -1
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Helpful",
                            tint = if (isLiked) FreshGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isLiked) "Helpful ($helpfulCount)" else "Helpful ($helpfulCount)",
                            fontSize = 11.sp,
                            fontWeight = if (isLiked) FontWeight.Bold else FontWeight.Medium,
                            color = if (isLiked) FreshGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Write & Submit Review Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WriteReviewBottomSheet(
    restaurant: RestaurantEntity,
    onDismiss: () -> Unit,
    onSubmit: (rating: Float, comment: String, dietaryTagsUsed: String, userName: String) -> Unit,
    initialUserName: String = "Alex Chen"
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var rating by remember { mutableFloatStateOf(5.0f) }
    var reviewText by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf(initialUserName) }
    val selectedDietaryTags = remember { mutableStateListOf<String>() }

    // Pre-populate tags with restaurant dietary capabilities
    val availableTags = remember {
        val list = mutableListOf("Vegetarian", "Vegan", "Gluten-Free", "Halal", "Jain", "Nut-Free", "Dairy-Free", "Keto")
        if (restaurant.isPureVeg) list.add(0, "100% Pure Veg")
        list.distinct()
    }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rate & Review",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Star Rating Picker
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HOW WAS YOUR FOOD & EXPERIENCE?",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    InteractiveStarRatingBar(
                        rating = rating,
                        onRatingChange = {
                            rating = it
                            showError = false
                        },
                        starSize = 40.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dietary Experience Multi-Select
            Text(
                text = "WHICH DIETARY OPTIONS DID YOU ENJOY?",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                availableTags.forEach { tag ->
                    val isSelected = tag in selectedDietaryTags
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedDietaryTags.remove(tag) else selectedDietaryTags.add(tag)
                        },
                        label = { Text(tag, fontSize = 12.sp) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandOrange,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name Input
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Your Name / Display Alias") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("review_user_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOrange,
                    focusedLabelColor = BrandOrange
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Text Feedback Input
            OutlinedTextField(
                value = reviewText,
                onValueChange = {
                    if (it.length <= 500) {
                        reviewText = it
                        showError = false
                    }
                },
                label = { Text("Your Review & Feedback") },
                placeholder = {
                    Text("Describe taste, food safety, packaging, allergen handling, and overall delight...")
                },
                minLines = 4,
                maxLines = 6,
                shape = RoundedCornerShape(10.dp),
                isError = showError,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (showError) {
                            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(text = "Constructive feedback helps kitchens & fellow foodies")
                        }
                        Text(text = "${reviewText.length}/500")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("review_text_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOrange,
                    focusedLabelColor = BrandOrange
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    if (rating <= 0f) {
                        showError = true
                        errorMessage = "Please choose a star rating (1 to 5 stars)"
                    } else if (reviewText.trim().length < 5) {
                        showError = true
                        errorMessage = "Please enter at least a short sentence of feedback"
                    } else {
                        onSubmit(
                            rating,
                            reviewText.trim(),
                            selectedDietaryTags.joinToString(", "),
                            userName.trim()
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_review_confirm_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RateReview,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Submit Review (${rating.toInt()} ★)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}
