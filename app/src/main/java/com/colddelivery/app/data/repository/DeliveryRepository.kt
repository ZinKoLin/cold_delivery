package com.colddelivery.app.data.repository

import androidx.room.withTransaction
import com.colddelivery.app.core.fifo.FifoBatch
import com.colddelivery.app.core.fifo.FifoStockManager
import com.colddelivery.app.data.local.database.ColdDeliveryDatabase
import com.colddelivery.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface DeliveryRepository {
    fun observeProducts(): Flow<List<ProductEntity>>
    fun observeCustomers(): Flow<List<CustomerEntity>>
    fun observeCustomersForDay(day: DeliveryDay): Flow<List<CustomerEntity>>
    fun observeHistory(): Flow<List<DeliveryEntity>>
    fun observeDeliveredCustomerIds(date: Long): Flow<List<Long>>
    fun observeTotalStock(): Flow<Int>
    suspend fun saveDelivered(customerId: Long, date: Long, items: List<Pair<Long, Int>>, now: Long): Long
    suspend fun undo(deliveryId: Long)
    suspend fun editDelivered(deliveryId: Long, items: List<Pair<Long, Int>>, now: Long)
}

class RoomDeliveryRepository @Inject constructor(private val db: ColdDeliveryDatabase, private val fifo: FifoStockManager) : DeliveryRepository {
    override fun observeProducts() = db.productDao().observeActive()
    override fun observeCustomers() = db.customerDao().observeActive()
    override fun observeCustomersForDay(day: DeliveryDay) = db.customerDao().observeForDay(day.name)
    override fun observeHistory() = db.deliveryDao().observeHistory()
    override fun observeDeliveredCustomerIds(date: Long) = db.deliveryDao().observeDeliveredCustomerIds(date)
    override fun observeTotalStock() = db.stockBatchDao().observeTotal()

    private suspend fun restoreItems(deliveryId: Long) {
        db.deliveryItemDao().forDelivery(deliveryId).forEach { item ->
            db.fifoAllocationDao().forItem(item.id).forEach { allocation ->
                val batch = db.stockBatchDao().get(allocation.stockBatchId) ?: error("Stock batch not found")
                db.stockBatchDao().update(batch.copy(remainingQty = batch.remainingQty + allocation.qtyUsed))
            }
            db.fifoAllocationDao().deleteForItem(item.id)
        }
        db.deliveryItemDao().deleteForDelivery(deliveryId)
    }

    private suspend fun allocate(deliveryId: Long, date: Long, items: List<Pair<Long, Int>>, now: Long): Pair<Int, Long> {
        require(items.isNotEmpty()) { "Delivery must contain at least one item" }
        require(items.map { it.first }.distinct().size == items.size) { "A product may appear only once per delivery" }
        val allocations = items.map { (productId, qty) ->
            require(qty > 0) { "Quantity must be greater than zero" }
            check(db.productDao().get(productId)?.isActive == true) { "Invalid product" }
            val price = db.dailyPriceDao().get(productId, date) ?: error("Daily price is missing")
            val batches = db.stockBatchDao().availableFifo(productId)
            Triple(productId, price.price, fifo.allocate(batches.map { FifoBatch(it.id, it.remainingQty) }, qty))
        }
        allocations.forEach { (productId, price, used) ->
            val qty = used.sumOf { it.qtyUsed }
            val itemId = db.deliveryItemDao().insert(DeliveryItemEntity(deliveryId = deliveryId, productId = productId, qty = qty, unitPrice = price, amount = qty * price, createdAt = now))
            used.forEach { allocation ->
                val batch = db.stockBatchDao().get(allocation.batchId)!!
                db.stockBatchDao().update(batch.copy(remainingQty = batch.remainingQty - allocation.qtyUsed))
                db.fifoAllocationDao().insert(FifoAllocationEntity(deliveryItemId = itemId, stockBatchId = allocation.batchId, qtyUsed = allocation.qtyUsed, createdAt = now))
            }
        }
        return items.sumOf { it.second } to allocations.sumOf { (_, price, used) -> used.sumOf { it.qtyUsed } * price }
    }

    override suspend fun saveDelivered(customerId: Long, date: Long, items: List<Pair<Long, Int>>, now: Long): Long = db.withTransaction {
        check(db.customerDao().get(customerId)?.isActive == true) { "Invalid customer" }
        val existing = db.deliveryDao().forCustomerOnDate(customerId, date)
        check(existing == null || existing.status != DeliveryStatus.DELIVERED) { "Customer already delivered for this date" }
        val deliveryId = existing?.id ?: db.deliveryDao().insert(DeliveryEntity(customerId = customerId, deliveryDate = date, totalQty = 0, totalAmount = 0, createdAt = now, updatedAt = now))
        if (existing != null) restoreItems(deliveryId)
        val totals = allocate(deliveryId, date, items, now)
        db.deliveryDao().update((existing ?: db.deliveryDao().get(deliveryId)!!).copy(status = DeliveryStatus.DELIVERED, totalQty = totals.first, totalAmount = totals.second, updatedAt = now, isDeleted = false))
        deliveryId
    }

    override suspend fun undo(deliveryId: Long) = db.withTransaction {
        val delivery = db.deliveryDao().get(deliveryId) ?: error("Delivery not found")
        restoreItems(deliveryId)
        db.deliveryDao().update(delivery.copy(status = DeliveryStatus.PENDING, totalQty = 0, totalAmount = 0, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun editDelivered(deliveryId: Long, items: List<Pair<Long, Int>>, now: Long) = db.withTransaction {
        val existing = db.deliveryDao().get(deliveryId) ?: error("Delivery not found")
        restoreItems(deliveryId)
        val totals = allocate(deliveryId, existing.deliveryDate, items, now)
        db.deliveryDao().update(existing.copy(status = DeliveryStatus.DELIVERED, totalQty = totals.first, totalAmount = totals.second, updatedAt = now, isDeleted = false))
    }
}
