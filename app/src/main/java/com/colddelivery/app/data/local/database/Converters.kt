package com.colddelivery.app.data.local.database

import androidx.room.TypeConverter
import com.colddelivery.app.data.local.entity.DeliveryDay
import com.colddelivery.app.data.local.entity.DeliveryStatus

class Converters {
    @TypeConverter fun fromStatus(value: DeliveryStatus) = value.name
    @TypeConverter fun toStatus(value: String) = DeliveryStatus.valueOf(value)
    @TypeConverter fun fromDay(value: DeliveryDay) = value.name
    @TypeConverter fun toDay(value: String) = DeliveryDay.valueOf(value)
}
