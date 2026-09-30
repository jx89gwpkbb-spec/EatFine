package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DietaryFilterState
import com.example.data.model.DietaryRestriction
import com.example.data.model.SortOption
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.FreshGreen

@Composable
fun DietaryBadge(
    restriction: DietaryRestriction,
    modifier: Modifier = Modifier,
    useShortTag: Boolean = false
) {
    Surface(
        color = restriction.containerColor.copy(alpha = 0.85f),
        contentColor = restriction.primaryColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.8.dp, restriction.primaryColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = restriction.iconEmoji, fontSize = 11.sp)
            Text(
                text = if (useShortTag) restriction.shortTag else restriction.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = restriction.primaryColor
            )
        }
    }
}

@Composable
fun DietaryFilterPillsRow(
    selectedRestrictions: Set<DietaryRestriction>,
    onToggleRestriction: (DietaryRestriction) -> Unit,
    pureVegOnly: Boolean,
    onTogglePureVeg: () -> Unit,
    onOpenFilterSheet: () -> Unit,
    activeFilterCount: Int,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Filter button with badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (activeFilterCount > 0) BrandOrange else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (activeFilterCount > 0) Color.White else MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(
                1.dp,
                if (activeFilterCount > 0) BrandOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .clickable { onOpenFilterSheet() }
                .testTag("open_dietary_filters_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filters",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Filters",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (activeFilterCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$activeFilterCount",
                            color = BrandOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Pure Veg Pill
        val isVegActive = pureVegOnly
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isVegActive) FreshGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.2.dp,
                if (isVegActive) FreshGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .clickable { onTogglePureVeg() }
                .testTag("filter_pure_veg_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .border(1.5.dp, FreshGreen, RoundedCornerShape(2.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(FreshGreen, CircleShape)
                    )
                }
                Text(
                    text = "Pure Veg",
                    fontSize = 12.sp,
                    fontWeight = if (isVegActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isVegActive) FreshGreen else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Dietary Restriction Chips
        DietaryRestriction.entries.forEach { restriction ->
            val isSelected = restriction in selectedRestrictions
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) restriction.containerColor else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.2.dp,
                    if (isSelected) restriction.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                ),
                modifier = Modifier
                    .clickable { onToggleRestriction(restriction) }
                    .testTag("filter_dietary_${restriction.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(text = restriction.iconEmoji, fontSize = 13.sp)
                    Text(
                        text = restriction.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) restriction.primaryColor else MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = restriction.primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DietaryFilterBottomSheet(
    filterState: DietaryFilterState,
    onToggleRestriction: (DietaryRestriction) -> Unit,
    onTogglePureVeg: () -> Unit,
    onSetMatchAll: (Boolean) -> Unit,
    onSetSort: (SortOption) -> Unit,
    onSetMinRating: (Float) -> Unit,
    onSetMaxDeliveryTime: (Int) -> Unit,
    onReset: () -> Unit,
    matchingCount: Int,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dietary & Food Filters",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Discover kitchens meeting your exact nutrition goals",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = onReset,
                    modifier = Modifier.testTag("reset_filters_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dietary Restrictions Section
            Text(
                text = "DIETARY RESTRICTIONS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = BrandOrange
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DietaryRestriction.entries.forEach { restriction ->
                    val isSelected = restriction in filterState.selectedRestrictions
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) restriction.containerColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) restriction.primaryColor else Color.Transparent
                        ),
                        modifier = Modifier
                            .clickable { onToggleRestriction(restriction) }
                            .testTag("sheet_dietary_${restriction.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = restriction.iconEmoji, fontSize = 14.sp)
                            Column {
                                Text(
                                    text = restriction.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) restriction.primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Match Strictness (Match Any vs Match All)
            if (filterState.selectedRestrictions.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Strict Match (All Selected)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (filterState.matchAllSelected)
                                "Show restaurants matching EVERY selected restriction"
                            else "Show restaurants matching ANY of the selected restrictions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = filterState.matchAllSelected,
                        onCheckedChange = { onSetMatchAll(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pure Vegetarian Kitchen Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FreshGreen.copy(alpha = 0.08f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Pure Vegetarian Kitchens Only",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FreshGreen
                        )
                    }
                    Text(
                        text = "100% Meatless and seafood-free facilities",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = filterState.pureVegOnly,
                    onCheckedChange = { onTogglePureVeg() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sort By Options
            Text(
                text = "SORT BY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = BrandOrange
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOption.entries.forEach { sort ->
                    val isSelected = filterState.sortBy == sort
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetSort(sort) },
                        label = { Text(sort.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandOrange.copy(alpha = 0.15f),
                            selectedLabelColor = BrandOrange
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Apply Button with live matching count
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("apply_dietary_filters_button")
            ) {
                Text(
                    text = "Show $matchingCount Restaurants",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
