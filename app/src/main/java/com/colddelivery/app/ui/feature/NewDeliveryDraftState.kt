package com.colddelivery.app.ui.feature

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue

internal data class DeliveryDraftItem(
    val productId: Long,
    val quantityText: String,
) {
    val quantity: Int get() = quantityText.toIntOrNull() ?: 0
}

internal data class DeliveryDraftTotals(
    val quantity: Int,
    val amount: Long,
)

internal class NewDeliveryDraftState {
    var customerId by mutableLongStateOf(0L)
        private set

    val items = mutableStateListOf<DeliveryDraftItem>()

    fun selectCustomer(id: Long) {
        if (customerId == id) return
        customerId = id
        items.clear()
    }

    fun addProduct(productId: Long) {
        if (items.none { it.productId == productId }) {
            items.add(DeliveryDraftItem(productId, "1"))
        }
    }

    fun updateQuantity(productId: Long, value: String) {
        val index = items.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            items[index] = items[index].copy(quantityText = value.filter(Char::isDigit))
        }
    }

    fun removeProduct(productId: Long) {
        items.removeAll { it.productId == productId }
    }

    fun totals(prices: Map<Long, Long>): DeliveryDraftTotals = DeliveryDraftTotals(
        quantity = items.sumOf { it.quantity },
        amount = items.sumOf { it.quantity.toLong() * (prices[it.productId] ?: 0L) },
    )

    fun saveItems(): List<Pair<Long, Int>> = items.map { it.productId to it.quantity }

    fun reset() {
        customerId = 0L
        items.clear()
    }
}
