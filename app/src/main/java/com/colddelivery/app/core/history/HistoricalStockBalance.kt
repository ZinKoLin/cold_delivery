package com.colddelivery.app.core.history

data class HistoricalStockBalance(
    val productId: Long,
    val openingStock: Int,
    val stockIn: Int,
    val delivered: Int,
) {
    val closingStock: Int get() = openingStock + stockIn - delivered
}

fun requireHistoricalDate(date: Long, today: Long) { require(date <= today) { "Future dates are not allowed" } }

fun historicalBalance(openingStock: Int, stockIn: Int, delivered: Int): HistoricalStockBalance = HistoricalStockBalance(0L, openingStock, stockIn, delivered)
