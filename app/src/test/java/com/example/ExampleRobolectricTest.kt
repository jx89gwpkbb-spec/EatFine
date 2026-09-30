package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DietaryFilterState
import com.example.data.model.DietaryRestriction
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.repository.SeedData
import com.example.ui.components.getOrderStatusDescription
import com.example.ui.components.getOrderStatusProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("EatFine", appName)
  }

  @Test
  fun `test dietary restrictions filtering`() {
    val restaurants = SeedData.sampleRestaurants
    assertTrue("Restaurants seed data should not be empty", restaurants.isNotEmpty())

    // Test pure veg filter
    val pureVegKitchens = restaurants.filter { it.isPureVeg }
    assertTrue("Should have pure veg restaurants", pureVegKitchens.isNotEmpty())
    assertTrue("All pure veg restaurants must have isPureVeg true", pureVegKitchens.all { it.isPureVeg })

    // Test Vegan restriction filter
    val veganKitchens = restaurants.filter { rest ->
      DietaryRestriction.VEGAN in rest.dietaryList
    }
    assertTrue("Should find kitchens offering Vegan meals", veganKitchens.isNotEmpty())

    // Test Gluten-Free restriction filter
    val glutenFreeKitchens = restaurants.filter { rest ->
      DietaryRestriction.GLUTEN_FREE in rest.dietaryList
    }
    assertTrue("Should find kitchens offering Gluten-Free options", glutenFreeKitchens.isNotEmpty())

    // Test Halal restriction filter
    val halalKitchens = restaurants.filter { rest ->
      DietaryRestriction.HALAL in rest.dietaryList
    }
    assertTrue("Should find kitchens offering Halal options", halalKitchens.isNotEmpty())

    // Test Jain restriction filter
    val jainKitchens = restaurants.filter { rest ->
      DietaryRestriction.JAIN in rest.dietaryList
    }
    assertTrue("Should find kitchens offering Jain meals", jainKitchens.isNotEmpty())
  }

  @Test
  fun `test real time order tracking progress bar values`() {
    // Check progress percentage increases monotonically across delivery pipeline
    val progressPlaced = getOrderStatusProgress(OrderStatus.PLACED)
    val progressAccepted = getOrderStatusProgress(OrderStatus.ACCEPTED)
    val progressPreparing = getOrderStatusProgress(OrderStatus.PREPARING)
    val progressReady = getOrderStatusProgress(OrderStatus.READY_FOR_PICKUP)
    val progressOnTheWay = getOrderStatusProgress(OrderStatus.ON_THE_WAY)
    val progressDelivered = getOrderStatusProgress(OrderStatus.DELIVERED)
    val progressCancelled = getOrderStatusProgress(OrderStatus.CANCELLED)

    assertEquals(0.0f, progressCancelled, 0.001f)
    assertTrue("Placed progress should be > 0", progressPlaced > 0f)
    assertTrue("Accepted should be > Placed", progressAccepted > progressPlaced)
    assertTrue("Preparing should be > Accepted", progressPreparing > progressAccepted)
    assertTrue("Ready should be > Preparing", progressReady > progressPreparing)
    assertTrue("On The Way should be > Ready", progressOnTheWay > progressReady)
    assertEquals(1.0f, progressDelivered, 0.001f)
  }

  @Test
  fun `test real time order tracking status descriptions`() {
    val sampleOrder = SeedData.sampleOngoingOrder
    assertNotNull("Sample ongoing order should be present", sampleOrder)

    val descPlaced = getOrderStatusDescription(OrderStatus.PLACED, sampleOrder.restaurantName, sampleOrder.driverName)
    assertTrue(descPlaced.contains(sampleOrder.restaurantName))

    val descOnTheWay = getOrderStatusDescription(OrderStatus.ON_THE_WAY, sampleOrder.restaurantName, sampleOrder.driverName)
    assertTrue(descOnTheWay.contains(sampleOrder.driverName))

    val descDelivered = getOrderStatusDescription(OrderStatus.DELIVERED, sampleOrder.restaurantName, sampleOrder.driverName)
    assertTrue(descDelivered.contains("delivered", ignoreCase = true))
  }

  @Test
  fun `test sample ongoing order initial state and ETA`() {
    val ongoing = SeedData.sampleOngoingOrder
    assertEquals(OrderStatus.ON_THE_WAY, ongoing.status)
    assertTrue("ETA should be greater than 0", ongoing.estimatedDeliveryMinutes > 0)
    assertTrue("Ongoing order should have non-empty restaurant name", ongoing.restaurantName.isNotBlank())
    assertTrue("Ongoing order should have assigned driver", ongoing.driverName.isNotBlank())
  }

  @Test
  fun `test restaurant review system data and rating aggregation`() {
    val reviews = SeedData.sampleReviews
    assertTrue("Sample reviews must not be empty", reviews.isNotEmpty())

    // Validate review data fields
    reviews.forEach { r ->
      assertTrue("Review rating must be between 1 and 5", r.rating in 1.0f..5.0f)
      assertTrue("Review text comment must not be empty", r.comment.isNotBlank())
      assertTrue("Review user name must not be empty", r.userName.isNotBlank())
      assertTrue("Review must belong to a restaurant", r.restaurantId.isNotBlank())
    }

    // Verify rating average calculation for Verde & Grain
    val verdeReviews = reviews.filter { it.restaurantId == "rest_verde" }
    assertTrue("Verde & Grain should have reviews", verdeReviews.isNotEmpty())
    val avgRating = verdeReviews.map { it.rating }.average()
    assertTrue("Average rating should be between 4 and 5", avgRating in 4.0..5.0)

    // Simulate adding a new 5-star review and testing rounded average update
    val newReviewRating = 5.0f
    val allRatings = verdeReviews.map { it.rating } + newReviewRating
    val newAvg = allRatings.average()
    val rounded = (Math.round(newAvg * 10.0) / 10.0).toFloat()
    assertTrue("Updated average should be accurate", rounded >= 4.5f)
  }

  @Test
  fun `test reviews contain dietary safety tags`() {
    val reviews = SeedData.sampleReviews
    val reviewsWithDietaryTags = reviews.filter { it.dietaryTagsUsed.isNotBlank() }
    assertTrue("Reviews should include dietary tags tested by customers", reviewsWithDietaryTags.isNotEmpty())

    // Ensure common dietary preferences are captured in reviews
    val allTagsCombined = reviewsWithDietaryTags.joinToString(", ") { it.dietaryTagsUsed }
    assertTrue("Reviews should mention Vegan tag", allTagsCombined.contains("Vegan", ignoreCase = true))
    assertTrue("Reviews should mention Gluten-Free tag", allTagsCombined.contains("Gluten-Free", ignoreCase = true))
    assertTrue("Reviews should mention Halal tag", allTagsCombined.contains("Halal", ignoreCase = true))
  }

  @Test
  fun `test favorite bookmarking and filtering in dedicated list`() {
    val allRestaurants = SeedData.sampleRestaurants
    val initialFavIds = SeedData.initialFavoriteRestaurantIds.toSet()
    assertTrue("Initial favorites list should not be empty", initialFavIds.isNotEmpty())

    // Filter restaurants that are in favorites
    val bookmarkedRestaurants = allRestaurants.filter { it.id in initialFavIds }
    assertEquals(initialFavIds.size, bookmarkedRestaurants.size)

    // Test bookmark toggling
    val mutableFavorites = initialFavIds.toMutableSet()
    val testRestaurantId = "rest_bliss"
    assertTrue("Test restaurant should not be initial favorite", testRestaurantId !in mutableFavorites)

    // Add bookmark
    mutableFavorites.add(testRestaurantId)
    assertTrue("Test restaurant is now bookmarked", testRestaurantId in mutableFavorites)

    // Remove bookmark
    mutableFavorites.remove(testRestaurantId)
    assertTrue("Test restaurant is removed from bookmarks", testRestaurantId !in mutableFavorites)

    // Test dietary filtering inside bookmarked list
    val veganBookmarks = bookmarkedRestaurants.filter { DietaryRestriction.VEGAN in it.dietaryList }
    assertTrue("Should have vegan restaurants in favorites", veganBookmarks.isNotEmpty())
    assertTrue("All vegan bookmarks must support vegan standard", veganBookmarks.all { DietaryRestriction.VEGAN in it.dietaryList })

    // Test sorting bookmarked list by rating
    val sortedByRating = bookmarkedRestaurants.sortedByDescending { it.rating }
    if (sortedByRating.size >= 2) {
      assertTrue("First item should have rating >= second item", sortedByRating[0].rating >= sortedByRating[1].rating)
    }
  }

  @Test
  fun `test order history listing with total cost and items`() {
    val pastOrders = SeedData.samplePastOrders
    assertTrue("Sample past orders must not be empty", pastOrders.isNotEmpty())

    pastOrders.forEach { order ->
      assertTrue("Order ID must not be blank", order.orderId.isNotBlank())
      assertTrue("Restaurant Name must not be blank", order.restaurantName.isNotBlank())
      assertTrue("Total cost must be positive", order.totalAmount > 0.0)
      assertTrue("Subtotal must be positive", order.subtotal > 0.0)
      assertTrue("Items summary must not be blank", order.itemsSummary.isNotBlank())
      assertTrue("Items summary should contain quantity indicator 'x '", order.itemsSummary.contains("x "))
      assertEquals(OrderStatus.DELIVERED, order.status)
      assertTrue("Placed timestamp must be valid", order.placedTimestamp > 0L)
    }
  }

  @Test
  fun `test single-click reorder duplicates order correctly`() {
    val pastOrder = SeedData.samplePastOrders.first()
    val clonedOrderId = "EF-" + (100000..999999).random()
    val reordered = pastOrder.copy(
      orderId = clonedOrderId,
      status = OrderStatus.PLACED,
      placedTimestamp = System.currentTimeMillis()
    )

    assertNotEquals("Cloned order must have a fresh unique ID", pastOrder.orderId, reordered.orderId)
    assertEquals(OrderStatus.PLACED, reordered.status)
    assertEquals(pastOrder.restaurantId, reordered.restaurantId)
    assertEquals(pastOrder.restaurantName, reordered.restaurantName)
    assertEquals(pastOrder.totalAmount, reordered.totalAmount, 0.001)
    assertEquals(pastOrder.itemsSummary, reordered.itemsSummary)
    assertEquals(pastOrder.chefDietaryInstructions, reordered.chefDietaryInstructions)
  }
}
