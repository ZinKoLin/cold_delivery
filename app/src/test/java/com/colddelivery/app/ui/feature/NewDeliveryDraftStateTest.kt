package com.colddelivery.app.ui.feature

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewDeliveryDraftStateTest {
    private val prices = mapOf(1L to 35_000L, 2L to 36_000L, 3L to 33_500L)

    @Test
    fun twoVisibleItemsProduceExactTotals() {
        val draft = NewDeliveryDraftState()
        draft.selectCustomer(10L)
        draft.addProduct(1L)
        draft.updateQuantity(1L, "10")
        draft.addProduct(2L)
        draft.updateQuantity(2L, "5")

        assertEquals(DeliveryDraftTotals(15, 530_000L), draft.totals(prices))
    }

    @Test
    fun removedItemNoLongerContributesToTotals() {
        val draft = populatedDraft()
        draft.addProduct(3L)
        assertEquals(DeliveryDraftTotals(16, 563_500L), draft.totals(prices))

        draft.removeProduct(3L)

        assertEquals(DeliveryDraftTotals(15, 530_000L), draft.totals(prices))
    }

    @Test
    fun quantityChangeImmediatelyUpdatesTotals() {
        val draft = populatedDraft()

        draft.updateQuantity(2L, "2")

        assertEquals(DeliveryDraftTotals(12, 422_000L), draft.totals(prices))
    }

    @Test
    fun freshDraftAndCustomerChangeContainNoStaleItems() {
        val previous = populatedDraft()
        previous.selectCustomer(20L)
        assertTrue(previous.items.isEmpty())
        assertEquals(DeliveryDraftTotals(0, 0L), previous.totals(prices))

        val fresh = NewDeliveryDraftState()
        assertTrue(fresh.items.isEmpty())
        assertEquals(DeliveryDraftTotals(0, 0L), fresh.totals(prices))
    }

    private fun populatedDraft() = NewDeliveryDraftState().apply {
        selectCustomer(10L)
        addProduct(1L)
        updateQuantity(1L, "10")
        addProduct(2L)
        updateQuantity(2L, "5")
    }
}
