package com.colddelivery.app.core.history

import com.colddelivery.app.data.local.entity.*
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryFilterTest {
    private val customer = CustomerEntity(1, "A", "", "", DeliveryDay.MONDAY, "", createdAt = 1)
    private val other = CustomerEntity(2, "B", "", "", DeliveryDay.TUESDAY, "", createdAt = 1)
    private val deliveries = listOf(
        DeliveryEntity(1, 1, 100, DeliveryStatus.DELIVERED, 2, 20, 1, 1),
        DeliveryEntity(2, 2, 100, DeliveryStatus.DELIVERED, 3, 30, 1, 1),
        DeliveryEntity(3, 1, 99, DeliveryStatus.DELIVERED, 1, 10, 1, 1),
    )

    @Test fun `customer product day and date criteria combine`() {
        val result = filterHistory(deliveries, listOf(customer, other), mapOf(1L to setOf(7L), 2L to setOf(8L), 3L to setOf(7L)), 100, HistoryCriteria("Date", 100, 1, 7, DeliveryDay.MONDAY))
        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test fun `product filter uses delivery item relationship`() {
        val result = filterHistory(deliveries, listOf(customer, other), mapOf(1L to setOf(7L), 2L to setOf(8L)), 100, HistoryCriteria(productId = 8L))
        assertEquals(listOf(2L), result.map { it.id })
    }

    @Test fun `delivery day filter uses customer`() {
        val result = filterHistory(deliveries, listOf(customer, other), emptyMap(), 100, HistoryCriteria(deliveryDay = DeliveryDay.TUESDAY))
        assertEquals(listOf(2L), result.map { it.id })
    }
}
