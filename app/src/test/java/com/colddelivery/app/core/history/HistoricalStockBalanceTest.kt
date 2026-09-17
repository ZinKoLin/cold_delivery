package com.colddelivery.app.core.history

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoricalStockBalanceTest {
    @Test fun `closing stock is reconstructed from opening movements`() {
        assertEquals(50, historicalBalance(0, 50, 0).closingStock)
        assertEquals(55, historicalBalance(50, 20, 15).closingStock)
        assertEquals(45, historicalBalance(55, 0, 10).closingStock)
    }

    @Test fun `same-day batches and deliveries aggregate`() {
        assertEquals(42, historicalBalance(30, 20 + 5, 10 + 3).closingStock)
    }

    @Test fun `card and detail use identical balance values for the same product and date`() {
        val balance = historicalBalance(150, 0, 10)

        assertEquals(150, balance.openingStock)
        assertEquals(0, balance.stockIn)
        assertEquals(10, balance.delivered)
        assertEquals(140, balance.closingStock)
    }

    @Test fun `zero stock and zero movement remain visible as zero`() {
        assertEquals(0, historicalBalance(0, 0, 0).closingStock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `future historical dates are rejected`() {
        requireHistoricalDate(11, 10)
    }}
