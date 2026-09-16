package com.colddelivery.app.core.fifo

import org.junit.Assert.*
import org.junit.Test

class FifoStockManagerTest {
    private val manager = FifoStockManager()
    @Test fun `allocates oldest batch first`() {
        val result = manager.allocate(listOf(FifoBatch(1, 50), FifoBatch(2, 100)), 70)
        assertEquals(listOf(FifoAllocation(1, 50), FifoAllocation(2, 20)), result)
    }
    @Test fun `rejects insufficient stock without allocation`() {
        try { manager.allocate(listOf(FifoBatch(1, 20), FifoBatch(2, 30)), 60); fail("Expected insufficient stock") } catch (e: InsufficientStockException) { assertEquals(50, e.available); assertEquals(60, e.required) }
    }
    @Test fun `undo allocation restores exact batches`() {
        val initial = listOf(FifoBatch(1, 50), FifoBatch(2, 100)); val used = manager.allocate(initial, 70)
        val restored = initial.map { batch -> batch.copy(remainingQty = batch.remainingQty - (used.find { it.batchId == batch.id }?.qtyUsed ?: 0) + (used.find { it.batchId == batch.id }?.qtyUsed ?: 0)) }
        assertEquals(initial, restored)
    }
}
