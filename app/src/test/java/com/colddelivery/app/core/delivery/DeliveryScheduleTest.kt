package com.colddelivery.app.core.delivery

import com.colddelivery.app.data.local.entity.CustomerEntity
import com.colddelivery.app.data.local.entity.DeliveryDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DeliveryScheduleTest {
    private fun customer(name: String, day: DeliveryDay, order: Int, active: Boolean = true) =
        CustomerEntity(
            name = name,
            phone = "09",
            address = "Yangon",
            deliveryDay = day,
            note = "",
            sortOrder = order,
            isActive = active,
            createdAt = 0L,
        )

    private val customers = listOf(
        customer("Tuesday", DeliveryDay.TUESDAY, 1),
        customer("Wednesday late", DeliveryDay.WEDNESDAY, 2),
        customer("Wednesday archived", DeliveryDay.WEDNESDAY, 0, active = false),
        customer("Wednesday first", DeliveryDay.WEDNESDAY, 1),
        customer("Thursday", DeliveryDay.THURSDAY, 1),
    )

    @Test fun wednesdayIncludesOnlyActiveWednesdayCustomersInOrder() {
        assertEquals(
            listOf("Wednesday first", "Wednesday late"),
            customersScheduledForToday(customers, LocalDate.of(2026, 9, 16)).map { it.name },
        )
    }

    @Test fun tuesdayDoesNotIncludeOtherDeliveryDays() {
        assertEquals(listOf("Tuesday"), customersScheduledForToday(customers, LocalDate.of(2026, 9, 15)).map { it.name })
    }

    @Test fun sundayIsClosedAndHasNoScheduledCustomers() {
        assertNull(deliveryDayFor(LocalDate.of(2026, 9, 13)))
        assertEquals(emptyList<CustomerEntity>(), customersScheduledForToday(customers, LocalDate.of(2026, 9, 13)))
    }
}
