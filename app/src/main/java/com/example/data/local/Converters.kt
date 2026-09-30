package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.DeliveryType
import com.example.data.model.OrderStatus

class Converters {
    @TypeConverter
    fun fromOrderStatus(status: OrderStatus?): String? = status?.name

    @TypeConverter
    fun toOrderStatus(name: String?): OrderStatus? =
        name?.let { runCatching { OrderStatus.valueOf(it) }.getOrDefault(OrderStatus.PLACED) }

    @TypeConverter
    fun fromDeliveryType(type: DeliveryType?): String? = type?.name

    @TypeConverter
    fun toDeliveryType(name: String?): DeliveryType? =
        name?.let { runCatching { DeliveryType.valueOf(it) }.getOrDefault(DeliveryType.DELIVERY) }
}
