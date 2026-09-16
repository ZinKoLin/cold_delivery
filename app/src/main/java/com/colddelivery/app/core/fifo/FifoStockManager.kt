package com.colddelivery.app.core.fifo

data class FifoBatch(val id: Long, val remainingQty: Int)
data class FifoAllocation(val batchId: Long, val qtyUsed: Int)
class InsufficientStockException(val available: Int, val required: Int) : IllegalStateException("Not enough stock. Available: $available, Required: $required")

/** Pure, oldest-first allocation logic. Persistence and transaction boundaries belong to the repository. */
class FifoStockManager {
    fun allocate(batchesOldestFirst: List<FifoBatch>, required: Int): List<FifoAllocation> {
        require(required > 0) { "Quantity must be greater than zero" }
        val available = batchesOldestFirst.sumOf { it.remainingQty }
        if (available < required) throw InsufficientStockException(available, required)
        var left = required
        return batchesOldestFirst.asSequence().mapNotNull { batch ->
            if (left == 0 || batch.remainingQty <= 0) return@mapNotNull null
            val used = minOf(left, batch.remainingQty); left -= used; FifoAllocation(batch.id, used)
        }.toList()
    }
}
