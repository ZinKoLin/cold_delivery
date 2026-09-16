package com.colddelivery.app.core.delivery

import com.colddelivery.app.data.local.entity.CustomerEntity
import com.colddelivery.app.data.local.entity.DeliveryDay
import java.time.DayOfWeek
import java.time.LocalDate

/** Maps the device-local date to the only delivery day that may be served today. */
fun deliveryDayFor(date: LocalDate): DeliveryDay? =
    if (date.dayOfWeek == DayOfWeek.SUNDAY) null
    else DeliveryDay.valueOf(date.dayOfWeek.name)

fun customersScheduledForToday(
    customers: List<CustomerEntity>,
    date: LocalDate,
): List<CustomerEntity> {
    val day = deliveryDayFor(date) ?: return emptyList()
    return customers
        .asSequence()
        .filter { it.isActive && it.deliveryDay == day }
        .sortedBy { it.sortOrder }
        .toList()
}
