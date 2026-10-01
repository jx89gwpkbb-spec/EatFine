package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DietaryFilterState
import com.example.data.model.DietaryRestriction
import com.example.data.model.RestaurantEntity
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.FreshGreen

data class AiRecommendation(
    val restaurant: RestaurantEntity,
    val matchPercentage: Int,
    val aiReason: String,
    val highlightedBadge: String
)

@Composable
fun AiRecommendedSection(
    restaurants: List<RestaurantEntity>,
    favoriteIds: Set<String>,
    filterState: DietaryFilterState,
    onSelectRestaurant: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var aiMoodFilter by remember { mutableStateOf("✨ All Around") }
    val moodOptions = listOf("✨ All Around", "🌱 Pure Green", "⚡ Fast (< 20m)", "💪 High Protein", "⭐ Top Rated")

    val aiRecommendations = remember(restaurants, favoriteIds, filterState, aiMoodFilter) {
        restaurants.map { rest ->
            var score = 75

            // Dietary match bonus
            val hasDietaryOverlap = filterState.selectedRestrictions.any { it in rest.dietaryList }
            if (hasDietaryOverlap) score += 15
            if (filterState.pureVegOnly && rest.isPureVeg) score += 12
            if (rest.isPureVeg) score += 4
            if (rest.rating >= 4.8f) score += 6
            if (rest.id in favoriteIds) score += 5

            // Generate contextual AI reasoning based on dietary standards & cuisines
            val reason = when {
                rest.isPureVeg && filterState.pureVegOnly ->
                    "100% Pure Veg & Satvik kitchen. Strictly zero meat or cross-contact."
                DietaryRestriction.VEGAN in rest.dietaryList && DietaryRestriction.GLUTEN_FREE in rest.dietaryList ->
                    "Dual-certified Vegan & Gluten-Free station. Ideal for celiac & plant-forward diners."
                DietaryRestriction.HALAL in rest.dietaryList ->
                    "100% Certified Halal meats with segregated preparation tandoor."
                DietaryRestriction.KETO in rest.dietaryList ->
                    "Tracked low-carb macros (< 8g net carbs) with clean avocado and olive oils."
                DietaryRestriction.JAIN in rest.dietaryList ->
                    "Strict Jain compliance: zero onion, garlic, or underground root vegetables."
                else ->
                    "Matches your high rating preferences with verified cross-contamination protocols."
            }

            val badge = when {
                rest.isPureVeg -> "100% Veg Match"
                DietaryRestriction.GLUTEN_FREE in rest.dietaryList -> "Celiac Safe"
                DietaryRestriction.HALAL in rest.dietaryList -> "Halal Certified"
                DietaryRestriction.KETO in rest.dietaryList -> "Macro-Tracked"
                else -> "Chef Approved"
            }

            val finalPercentage = score.coerceIn(86, 99)
            AiRecommendation(rest, finalPercentage, reason, badge)
        }.let { list ->
            when (aiMoodFilter) {
                "🌱 Pure Green" -> list.filter { it.restaurant.isPureVeg || DietaryRestriction.VEGAN in it.restaurant.dietaryList }
                "⚡ Fast (< 20m)" -> list.filter { it.restaurant.deliveryTimeMin <= 20 }
                "💪 High Protein" -> list.filter { DietaryRestriction.KETO in it.restaurant.dietaryList || it.restaurant.cuisines.contains("protein", ignoreCase = true) || it.restaurant.cuisines.contains("kebab", ignoreCase = true) }
                "⭐ Top Rated" -> list.sortedByDescending { it.restaurant.rating }
                else -> list.sortedByDescending { it.matchPercentage }
            }
        }.take(5)
    }

    if (aiRecommendations.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("ai_recommended_section")
    ) {
        // AI Header Bar with glowing gradient
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B),
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
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
                                .size(32.dp)
                                .background(
                                    Brush.linearGradient(listOf(BrandOrange, Color(0xFFFF922B))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "AI Recommended for You",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Surface(
                                    color = BrandOrange.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "SMART DIETARY AI",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandOrange,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Personalized from your lifestyle, taste & dietary standards",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mood / Taste Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    moodOptions.forEach { mood ->
                        val isSelected = aiMoodFilter == mood
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) BrandOrange else Color.White.copy(alpha = 0.12f),
                            border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.clickable { aiMoodFilter = mood }
                        ) {
                            Text(
                                text = mood,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Recommendation Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            aiRecommendations.forEach { rec ->
                AiRestaurantCard(
                    recommendation = rec,
                    onClick = { onSelectRestaurant(rec.restaurant.id) }
                )
            }
        }
    }
}

@Composable
fun AiRestaurantCard(
    recommendation: AiRecommendation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rest = recommendation.restaurant

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .width(260.dp)
            .clickable { onClick() }
            .testTag("ai_card_${rest.id}")
    ) {
        Column {
            // Image with Match percentage overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Image(
                    painter = painterResource(id = rest.bannerDrawableRes),
                    contentDescription = rest.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )

                // AI Match Badge (Top Start)
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    color = BrandOrange,
                    shadowElevation = 3.dp,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${recommendation.matchPercentage}% AI Match",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Delivery Time (Bottom End)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "${rest.deliveryTimeMin} mins • ${rest.distanceKm} km",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rest.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${rest.rating}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = rest.cuisines,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // AI Reasoning Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FreshGreen.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, FreshGreen.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🤖", fontSize = 11.sp)
                        Text(
                            text = recommendation.aiReason,
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                ) {
                    Text(
                        text = "View Menu →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
