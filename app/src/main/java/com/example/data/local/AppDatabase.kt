package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CartItemEntity
import com.example.data.model.FavoriteEntity
import com.example.data.model.MenuItemEntity
import com.example.data.model.OrderEntity
import com.example.data.model.ReservationEntity
import com.example.data.model.RestaurantEntity
import com.example.data.model.ReviewEntity

@Database(
    entities = [
        RestaurantEntity::class,
        MenuItemEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        ReservationEntity::class,
        FavoriteEntity::class,
        ReviewEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun reservationDao(): ReservationDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE cart_items ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE orders ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE reservations ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                database.execSQL(
                    "CREATE TABLE favorites_new (userId TEXT NOT NULL, restaurantId TEXT NOT NULL, savedTimestamp INTEGER NOT NULL, PRIMARY KEY(userId, restaurantId))"
                )
                database.execSQL(
                    "INSERT INTO favorites_new (userId, restaurantId, savedTimestamp) SELECT '', restaurantId, savedTimestamp FROM favorites"
                )
                database.execSQL("DROP TABLE favorites")
                database.execSQL("ALTER TABLE favorites_new RENAME TO favorites")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eatfine_database.db"
                ).addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
