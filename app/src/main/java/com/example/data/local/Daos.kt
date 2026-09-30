package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CartItemEntity
import com.example.data.model.FavoriteEntity
import com.example.data.model.MenuItemEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.ReservationEntity
import com.example.data.model.RestaurantEntity
import com.example.data.model.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE id = :id")
    fun getRestaurantById(id: String): Flow<RestaurantEntity?>

    @Query("SELECT * FROM restaurants WHERE id = :id")
    suspend fun getRestaurantByIdDirect(id: String): RestaurantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<RestaurantEntity>)

    @Update
    suspend fun updateRestaurant(restaurant: RestaurantEntity)

    @Query("SELECT COUNT(*) FROM restaurants")
    suspend fun getCount(): Int

    @Query("UPDATE restaurants SET rating = :rating, reviewCount = :reviewCount WHERE id = :restaurantId")
    suspend fun updateRestaurantRating(restaurantId: String, rating: Float, reviewCount: Int)
}

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items WHERE restaurantId = :restaurantId")
    fun getMenuItemsByRestaurant(restaurantId: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Query("UPDATE menu_items SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun setAvailability(id: String, isAvailable: Boolean)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE menuItemId = :menuItemId LIMIT 1")
    suspend fun getItemByMenuId(menuItemId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItemById(id: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY placedTimestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    fun getOrderById(orderId: String): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus)
}

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations ORDER BY bookedTimestamp DESC")
    fun getAllReservations(): Flow<List<ReservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity)

    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun deleteReservation(id: String)
}

@Dao
interface FavoriteDao {
    @Query("SELECT restaurantId FROM favorites")
    fun getFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE restaurantId = :restaurantId")
    suspend fun removeFavorite(restaurantId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE restaurantId = :restaurantId)")
    suspend fun isFavorite(restaurantId: String): Boolean
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE restaurantId = :restaurantId ORDER BY id DESC")
    fun getReviewsForRestaurant(restaurantId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("SELECT AVG(rating) FROM reviews WHERE restaurantId = :restaurantId")
    suspend fun getAverageRating(restaurantId: String): Float?

    @Query("SELECT COUNT(*) FROM reviews WHERE restaurantId = :restaurantId")
    suspend fun getReviewCount(restaurantId: String): Int
}
