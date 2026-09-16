package com.colddelivery.app.data.local.database

import androidx.room.withTransaction
import com.colddelivery.app.data.local.entity.*
import java.time.LocalDate
import com.colddelivery.app.core.auth.PasswordHasher

object DemoSeeder {
    suspend fun seed(db: ColdDeliveryDatabase) = db.withTransaction {
        val existingUser = db.userDao().get()
        if (existingUser == null || existingUser.passwordIterations <= 0 || existingUser.passwordSalt.isBlank()) {
            val stored = PasswordHasher.create("cold2026")
            db.userDao().upsert((existingUser ?: UserEntity(username = "admin", passwordHash = stored.hash)).copy(passwordHash = stored.hash, passwordSalt = stored.salt, passwordIterations = stored.iterations))
        }
        if (db.productDao().count() > 0) return@withTransaction
        val now = System.currentTimeMillis(); val today = LocalDate.now().toEpochDay()
        val products = listOf(
            ProductEntity(productCode = "C001", productName = "Coca-Cola 330ml", createdAt = now),
            ProductEntity(productCode = "S001", productName = "Sprite 330ml", createdAt = now),
            ProductEntity(productCode = "F001", productName = "Fanta 330ml", createdAt = now),
            ProductEntity(productCode = "SC01", productName = "Schweppes", createdAt = now)
        )
        val ids = db.productDao().insertAll(products)
        val coca = ids[0]; val sprite = ids[1]; val fanta = ids[2]; val schweppes = ids[3]
        db.stockBatchDao().insertAll(listOf(StockBatchEntity(productId = coca, stockInDate = today - 14, initialQty = 50, remainingQty = 40, createdAt = now), StockBatchEntity(productId = coca, stockInDate = today - 4, initialQty = 100, remainingQty = 100, createdAt = now), StockBatchEntity(productId = sprite, stockInDate = today - 7, initialQty = 60, remainingQty = 60, createdAt = now), StockBatchEntity(productId = fanta, stockInDate = today - 2, initialQty = 10, remainingQty = 10, createdAt = now), StockBatchEntity(productId = schweppes, stockInDate = today - 1, initialQty = 0, remainingQty = 0, createdAt = now)))
        db.dailyPriceDao().upsert(DailyPriceEntity(productId = coca, priceDate = today, price = 35000, createdAt = now, updatedAt = now)); db.dailyPriceDao().upsert(DailyPriceEntity(productId = sprite, priceDate = today, price = 34000, createdAt = now, updatedAt = now)); db.dailyPriceDao().upsert(DailyPriceEntity(productId = fanta, priceDate = today, price = 33500, createdAt = now, updatedAt = now)); db.dailyPriceDao().upsert(DailyPriceEntity(productId = schweppes, priceDate = today, price = 36000, createdAt = now, updatedAt = now))
        val customers = listOf(CustomerEntity(name = "ABC Store", phone = "09 421 234 567", address = "Bogyoke Market, Yangon", deliveryDay = DeliveryDay.MONDAY, note = "Main counter", sortOrder = 1, createdAt = now), CustomerEntity(name = "Shwe Moe Shop", phone = "09 777 345 678", address = "Hledan, Yangon", deliveryDay = DeliveryDay.MONDAY, note = "Call before delivery", sortOrder = 2, createdAt = now), CustomerEntity(name = "Mya Nandar Mart", phone = "09 250 456 789", address = "Tamwe, Yangon", deliveryDay = DeliveryDay.TUESDAY, note = "", sortOrder = 1, createdAt = now), CustomerEntity(name = "Royal Star Store", phone = "09 950 567 890", address = "North Dagon, Yangon", deliveryDay = DeliveryDay.WEDNESDAY, note = "", sortOrder = 1, createdAt = now))
        val customerIds = db.customerDao().insertAll(customers)
        val deliveredCustomer = customerIds[0]
        val deliveryId = db.deliveryDao().insert(DeliveryEntity(customerId = deliveredCustomer, deliveryDate = today, status = DeliveryStatus.DELIVERED, totalQty = 10, totalAmount = 350000, createdAt = now, updatedAt = now))
        val itemId = db.deliveryItemDao().insert(DeliveryItemEntity(deliveryId = deliveryId, productId = coca, qty = 10, unitPrice = 35000, amount = 350000, createdAt = now))
        val batch = db.stockBatchDao().availableFifo(coca).first()
        db.stockBatchDao().update(batch.copy(remainingQty = batch.remainingQty - 10))
        db.fifoAllocationDao().insert(FifoAllocationEntity(deliveryItemId = itemId, stockBatchId = batch.id, qtyUsed = 10, createdAt = now))
    }
}
