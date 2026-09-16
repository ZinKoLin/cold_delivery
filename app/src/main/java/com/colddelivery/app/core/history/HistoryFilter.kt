package com.colddelivery.app.core.history

import com.colddelivery.app.data.local.entity.CustomerEntity
import com.colddelivery.app.data.local.entity.DeliveryDay
import com.colddelivery.app.data.local.entity.DeliveryEntity

data class HistoryCriteria(
    val range: String = "All",
    val exactDate: Long? = null,
    val customerId: Long? = null,
    val productId: Long? = null,
    val deliveryDay: DeliveryDay? = null,
)

fun filterHistory(
    deliveries: List<DeliveryEntity>,
    customers: List<CustomerEntity>,
    itemProducts: Map<Long, Set<Long>>,
    today: Long,
    criteria: HistoryCriteria,
): List<DeliveryEntity> = deliveries.filter { delivery ->
    val dateMatches = when (criteria.range) {
        "Today" -> delivery.deliveryDate == today
        "Week" -> delivery.deliveryDate in (today - 6)..today
        "Month" -> delivery.deliveryDate in (today - 30)..today
        "Date" -> delivery.deliveryDate == criteria.exactDate
        else -> true
    }
    dateMatches && (criteria.customerId == null || delivery.customerId == criteria.customerId) &&
        (criteria.productId == null || itemProducts[delivery.id].orEmpty().contains(criteria.productId)) &&
        (criteria.deliveryDay == null || customers.firstOrNull { it.id == delivery.customerId }?.deliveryDay == criteria.deliveryDay)
}
