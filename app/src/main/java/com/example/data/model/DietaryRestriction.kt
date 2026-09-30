package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenContainer
import com.example.ui.theme.GlutenFreeBlue
import com.example.ui.theme.GlutenFreeContainer
import com.example.ui.theme.JainPurple
import com.example.ui.theme.JainPurpleContainer
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberContainer
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.BrandOrangeContainer

enum class DietaryRestriction(
    val label: String,
    val shortTag: String,
    val description: String,
    val iconEmoji: String,
    val primaryColor: Color,
    val containerColor: Color
) {
    VEGETARIAN(
        label = "Vegetarian",
        shortTag = "Veg",
        description = "No meat, fish or poultry",
        iconEmoji = "🌱",
        primaryColor = FreshGreen,
        containerColor = FreshGreenContainer
    ),
    VEGAN(
        label = "Vegan",
        shortTag = "Vegan",
        description = "100% plant-based, no animal byproducts",
        iconEmoji = "🌿",
        primaryColor = FreshGreen,
        containerColor = FreshGreenContainer
    ),
    GLUTEN_FREE(
        label = "Gluten-Free",
        shortTag = "GF",
        description = "Safe for celiac or gluten intolerance",
        iconEmoji = "🌾",
        primaryColor = GlutenFreeBlue,
        containerColor = GlutenFreeContainer
    ),
    HALAL(
        label = "Halal",
        shortTag = "Halal",
        description = "Certified Halal preparation",
        iconEmoji = "✨",
        primaryColor = WarmAmber,
        containerColor = WarmAmberContainer
    ),
    JAIN(
        label = "Jain Friendly",
        shortTag = "Jain",
        description = "No root vegetables (onion, garlic, potato)",
        iconEmoji = "🧅",
        primaryColor = JainPurple,
        containerColor = JainPurpleContainer
    ),
    DAIRY_FREE(
        label = "Dairy-Free",
        shortTag = "DF",
        description = "No milk, cheese, lactose or butter",
        iconEmoji = "🥛",
        primaryColor = GlutenFreeBlue,
        containerColor = GlutenFreeContainer
    ),
    NUT_FREE(
        label = "Nut-Free",
        shortTag = "Nut-Safe",
        description = "Prepared in peanut & tree nut safe kitchen",
        iconEmoji = "🥜",
        primaryColor = WarmAmber,
        containerColor = WarmAmberContainer
    ),
    KETO(
        label = "Keto / Low-Carb",
        shortTag = "Keto",
        description = "High protein & healthy fats, under 10g net carbs",
        iconEmoji = "🥑",
        primaryColor = BrandOrange,
        containerColor = BrandOrangeContainer
    ),
    ORGANIC(
        label = "Organic",
        shortTag = "Organic",
        description = "Certified pesticide-free natural ingredients",
        iconEmoji = "🍃",
        primaryColor = FreshGreen,
        containerColor = FreshGreenContainer
    );

    companion object {
        fun fromKey(name: String): DietaryRestriction? {
            return entries.find { it.name.equals(name, ignoreCase = true) }
        }
    }
}
