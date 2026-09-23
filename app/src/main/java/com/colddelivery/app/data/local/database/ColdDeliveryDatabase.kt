package com.colddelivery.app.data.local.database
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.colddelivery.app.data.local.dao.*
import com.colddelivery.app.data.local.entity.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
@Database(entities = [UserEntity::class, ProductEntity::class, StockInTicketEntity::class, StockBatchEntity::class, CustomerEntity::class, DailyPriceEntity::class, DeliveryEntity::class, DeliveryItemEntity::class, FifoAllocationEntity::class], version = 3, exportSchema = true)
@TypeConverters(Converters::class)
abstract class ColdDeliveryDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao; abstract fun productDao(): ProductDao; abstract fun stockInTicketDao(): StockInTicketDao; abstract fun stockBatchDao(): StockBatchDao
    abstract fun customerDao(): CustomerDao; abstract fun dailyPriceDao(): DailyPriceDao; abstract fun deliveryDao(): DeliveryDao
    abstract fun deliveryItemDao(): DeliveryItemDao; abstract fun fifoAllocationDao(): FifoAllocationDao
}
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE users ADD COLUMN passwordSalt TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE users ADD COLUMN passwordIterations INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_deliveries_customerId_deliveryDate ON deliveries(customerId, deliveryDate)")
    }
}
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS stock_in_tickets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ticketDate INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
        database.execSQL("ALTER TABLE stock_batches ADD COLUMN ticketId INTEGER")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_stock_batches_ticketId ON stock_batches(ticketId)")
    }
}
