package com.colddelivery.app.data.local.entity

import androidx.room.*

enum class DeliveryStatus { PENDING, DELIVERED }
enum class DeliveryDay { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY }

@Entity(tableName = "users") data class UserEntity(@PrimaryKey val id: Long = 1, val username: String, val passwordHash: String, val passwordSalt: String = "", val passwordIterations: Int = 0, val rememberMe: Boolean = false)

@Entity(tableName = "products", indices = [Index(value = ["productCode"], unique = true)]) data class ProductEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val productCode: String, val productName: String, val unit: String = "CARTON", val isActive: Boolean = true, val createdAt: Long)

@Entity(tableName = "stock_in_tickets") data class StockInTicketEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val ticketDate: Long, val createdAt: Long)

@Entity(tableName = "stock_batches", foreignKeys = [ForeignKey(entity = ProductEntity::class, parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.CASCADE)], indices = [Index("productId"), Index("stockInDate"), Index("ticketId")]) data class StockBatchEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val productId: Long, val stockInDate: Long, val initialQty: Int, val remainingQty: Int, val createdAt: Long, val ticketId: Long? = null)

@Entity(tableName = "customers", indices = [Index("deliveryDay"), Index("sortOrder")]) data class CustomerEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val phone: String, val address: String, val deliveryDay: DeliveryDay, val note: String, val sortOrder: Int = 0, val isActive: Boolean = true, val createdAt: Long)

@Entity(tableName = "daily_prices", foreignKeys = [ForeignKey(entity = ProductEntity::class, parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.CASCADE)], indices = [Index(value = ["productId", "priceDate"], unique = true)]) data class DailyPriceEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val productId: Long, val priceDate: Long, val price: Long, val createdAt: Long, val updatedAt: Long)

@Entity(tableName = "deliveries", foreignKeys = [ForeignKey(entity = CustomerEntity::class, parentColumns = ["id"], childColumns = ["customerId"])], indices = [Index("customerId"), Index("deliveryDate"), Index(value = ["customerId", "deliveryDate"], unique = true)]) data class DeliveryEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val customerId: Long, val deliveryDate: Long, val status: DeliveryStatus = DeliveryStatus.PENDING, val totalQty: Int, val totalAmount: Long, val createdAt: Long, val updatedAt: Long, val isDeleted: Boolean = false)

@Entity(tableName = "delivery_items", foreignKeys = [ForeignKey(entity = DeliveryEntity::class, parentColumns = ["id"], childColumns = ["deliveryId"], onDelete = ForeignKey.CASCADE), ForeignKey(entity = ProductEntity::class, parentColumns = ["id"], childColumns = ["productId"])], indices = [Index("deliveryId"), Index("productId")]) data class DeliveryItemEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val deliveryId: Long, val productId: Long, val qty: Int, val unitPrice: Long, val amount: Long, val createdAt: Long)

@Entity(tableName = "fifo_allocations", foreignKeys = [ForeignKey(entity = DeliveryItemEntity::class, parentColumns = ["id"], childColumns = ["deliveryItemId"], onDelete = ForeignKey.CASCADE), ForeignKey(entity = StockBatchEntity::class, parentColumns = ["id"], childColumns = ["stockBatchId"])], indices = [Index("deliveryItemId"), Index("stockBatchId")]) data class FifoAllocationEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val deliveryItemId: Long, val stockBatchId: Long, val qtyUsed: Int, val createdAt: Long)
