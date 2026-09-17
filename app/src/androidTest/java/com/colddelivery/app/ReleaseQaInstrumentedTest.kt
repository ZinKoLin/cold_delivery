package com.colddelivery.app

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.colddelivery.app.core.backup.BackupManager
import com.colddelivery.app.core.delivery.customersScheduledForToday
import com.colddelivery.app.core.delivery.deliveryDayFor
import com.colddelivery.app.core.fifo.FifoBatch
import com.colddelivery.app.core.fifo.FifoAllocation
import com.colddelivery.app.core.fifo.FifoStockManager
import com.colddelivery.app.data.local.database.ColdDeliveryDatabase
import com.colddelivery.app.data.local.database.DemoSeeder
import com.colddelivery.app.data.local.entity.*
import com.colddelivery.app.data.preferences.PreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ReleaseQaInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: ColdDeliveryDatabase

    @Before fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase("cold_delivery.db")
        db = Room.databaseBuilder(context, ColdDeliveryDatabase::class.java, "cold_delivery.db").allowMainThreadQueries().build()
    }
    @After fun tearDown() { db.close(); context.deleteDatabase("cold_delivery.db") }

    @Test fun stockInBatchesRemainSeparateAndFifoConsumesOldestFirst() = runBlocking {
        val productId = db.productDao().insert(ProductEntity(productCode = "QA01", productName = "QAProduct", createdAt = 1L))
        val batch1 = db.stockBatchDao().insert(StockBatchEntity(productId = productId, stockInDate = 100L, initialQty = 10, remainingQty = 10, createdAt = 1L))
        val batch2 = db.stockBatchDao().insert(StockBatchEntity(productId = productId, stockInDate = 200L, initialQty = 20, remainingQty = 20, createdAt = 2L))
        assertEquals(listOf(batch1, batch2), db.stockBatchDao().allForProduct(productId).map { it.id })
        assertEquals(30, db.stockBatchDao().total(productId))
        val allocations = FifoStockManager().allocate(listOf(FifoBatch(batch1, 10), FifoBatch(batch2, 20)), 15)
        assertEquals(listOf(FifoAllocation(batch1, 10), FifoAllocation(batch2, 5)), allocations)
        db.stockBatchDao().update(db.stockBatchDao().get(batch1)!!.copy(remainingQty = 0))
        db.stockBatchDao().update(db.stockBatchDao().get(batch2)!!.copy(remainingQty = 15))
        assertEquals(0, db.stockBatchDao().get(batch1)!!.remainingQty)
        assertEquals(15, db.stockBatchDao().get(batch2)!!.remainingQty)
        assertEquals(15, db.stockBatchDao().total(productId))
        assertTrue(runCatching { FifoStockManager().allocate(listOf(FifoBatch(batch2, 15)), 0) }.isFailure)
        assertTrue(runCatching { FifoStockManager().allocate(listOf(FifoBatch(batch2, 15)), -1) }.isFailure)
    }

    @Test fun customerCrudArchiveAndHistoryPreserveData() = runBlocking {
        val first = db.customerDao().insert(CustomerEntity(name = "QA Customer", phone = "1", address = "A", deliveryDay = DeliveryDay.MONDAY, note = "N", sortOrder = 1, createdAt = 1L))
        val second = db.customerDao().insert(CustomerEntity(name = "Second", phone = "2", address = "B", deliveryDay = DeliveryDay.MONDAY, note = "", sortOrder = 2, createdAt = 2L))
        db.customerDao().update(db.customerDao().get(first)!!.copy(name = "Edited", phone = "9", address = "Edited A", note = "Edited note", sortOrder = 2))
        db.customerDao().update(db.customerDao().get(second)!!.copy(sortOrder = 1))
        assertEquals(listOf(second, first), db.customerDao().orderedForDay("MONDAY").map { it.id })
        val edited = db.customerDao().get(first)!!
        val deliveryId = db.deliveryDao().insert(DeliveryEntity(customerId = first, deliveryDate = 100L, status = DeliveryStatus.DELIVERED, totalQty = 1, totalAmount = 1L, createdAt = 1L, updatedAt = 1L))
        db.customerDao().update(edited.copy(isActive = false))
        assertTrue(db.customerDao().observeActive().first().none { it.id == first })
        assertEquals(deliveryId, db.deliveryDao().get(deliveryId)!!.id)
        assertEquals("Edited", db.customerDao().get(first)!!.name)
    }

    @Test fun seedRunsOnceAndSundayIsClosed() = runBlocking {
        DemoSeeder.seed(db)
        val products = db.productDao().count()
        val customers = db.customerDao().observeActive().first().size
        DemoSeeder.seed(db)
        assertEquals(4, products)
        assertEquals(products, db.productDao().count())
        assertEquals(customers, db.customerDao().observeActive().first().size)
        assertNotNull(db.userDao().get())
        assertNull(deliveryDayFor(LocalDate.of(2026, 9, 13)))
        assertTrue(customersScheduledForToday(listOf(CustomerEntity(name = "Wed", phone = "", address = "", deliveryDay = DeliveryDay.WEDNESDAY, note = "", createdAt = 1L)), LocalDate.of(2026, 9, 13)).isEmpty())
    }

    @Test fun productCodeIsUniqueAndProductHasOnlyCartonFields() = runBlocking {
        val first = db.productDao().insert(ProductEntity(productCode = "QA01", productName = "QAProduct", createdAt = 1L))
        val duplicate = db.productDao().insert(ProductEntity(productCode = "QA01", productName = "Duplicate", createdAt = 2L))
        assertTrue(first > 0)
        assertEquals(-1L, duplicate)
        assertEquals("CARTON", db.productDao().get(first)!!.unit)
        val fields = ProductEntity::class.java.declaredFields.map { it.name }.toSet()
        assertFalse(fields.contains("category")); assertFalse(fields.contains("supplier")); assertFalse(fields.contains("buyingPrice")); assertFalse(fields.contains("defaultSellingPrice"))
    }

    @Test fun localizedReleaseQaLabelsResolve() {
        val keys = listOf("home", "customers", "todays_price", "new_delivery", "stock", "history", "voucher", "settings")
        val english = context.resources
        val myConfig = Configuration(english.configuration)
        myConfig.setLocale(Locale("my"))
        val myanmar = context.createConfigurationContext(myConfig).resources
        keys.forEach { key ->
            val id = english.getIdentifier(key, "string", context.packageName)
            assertTrue("missing English resource $key", id != 0)
            assertTrue("blank English resource $key", english.getString(id).isNotBlank())
            assertTrue("blank Myanmar resource $key", myanmar.getString(id).isNotBlank())
            assertNotEquals("missing Myanmar translation $key", english.getString(id), myanmar.getString(id))
        }
    }

    @Test fun productionBackupRestoreAndInvalidBackupSafety(): Unit = runBlocking {
        DemoSeeder.seed(db)
        val preferences = PreferencesStore(context)
        preferences.setLanguage("en"); preferences.setLowStockThreshold(10)
        val baselineCustomer = db.customerDao().get(1L)!!
        val baselineProductCount = db.productDao().count()
        val baselineBatches = db.stockBatchDao().allForProduct(1L)
        val backupFile = File(context.cacheDir, "release-qa-backup.zip").apply { delete() }
        val manager = BackupManager(context, db, preferences) {}
        assertTrue(manager.create(Uri.fromFile(backupFile)).isSuccess)
        assertTrue(backupFile.exists()); assertTrue(backupFile.length() > 0)
        db.customerDao().update(baselineCustomer.copy(name = "Modified Customer"))
        db.stockBatchDao().update(baselineBatches.first().copy(remainingQty = 1))
        preferences.setLanguage("my"); preferences.setLowStockThreshold(99)
        assertEquals("Modified Customer", db.customerDao().get(baselineCustomer.id)!!.name)
        assertEquals("my", preferences.exportSnapshot().language); assertEquals(99, preferences.exportSnapshot().lowStockThreshold)
        assertTrue(manager.restore(Uri.fromFile(backupFile)).isSuccess)
        db = Room.databaseBuilder(context, ColdDeliveryDatabase::class.java, "cold_delivery.db").allowMainThreadQueries().build()
        val restoredCustomers = db.customerDao().observeActive().first()
        assertTrue("restored customers=$restoredCustomers", restoredCustomers.any { it.id == baselineCustomer.id && it.name == baselineCustomer.name })
        assertEquals(baselineProductCount, db.productDao().count())
        assertEquals(baselineBatches.map { it.remainingQty }, db.stockBatchDao().allForProduct(baselineBatches.first().productId).map { it.remainingQty })
        assertEquals("en", preferences.exportSnapshot().language); assertEquals(10, preferences.exportSnapshot().lowStockThreshold)
        val invalid = File(context.cacheDir, "release-qa-invalid.zip").apply { writeText("corrupt") }
        val beforeInvalid = db.customerDao().get(baselineCustomer.id)!!.name
        assertTrue(BackupManager(context, db, preferences) {}.restore(Uri.fromFile(invalid)).isFailure)
        assertEquals(beforeInvalid, db.customerDao().get(baselineCustomer.id)!!.name)
        backupFile.delete(); invalid.delete()
    }
    @Test fun historicalStockQueriesReconstructDailyBalancesAndRespectUndoEditAndArchive(): Unit = runBlocking {
        val productId = db.productDao().insert(ProductEntity(productCode = "QA01", productName = "QAProduct", createdAt = 1L))
        val customerId = db.customerDao().insert(CustomerEntity(name = "Historical Customer", phone = "", address = "", deliveryDay = DeliveryDay.MONDAY, note = "", createdAt = 1L))
        db.stockBatchDao().insert(StockBatchEntity(productId = productId, stockInDate = 1L, initialQty = 50, remainingQty = 50, createdAt = 1L))
        db.stockBatchDao().insert(StockBatchEntity(productId = productId, stockInDate = 2L, initialQty = 15, remainingQty = 15, createdAt = 2L))
        db.stockBatchDao().insert(StockBatchEntity(productId = productId, stockInDate = 2L, initialQty = 5, remainingQty = 5, createdAt = 3L))
        val day2Delivery = db.deliveryDao().insert(DeliveryEntity(customerId = customerId, deliveryDate = 2L, status = DeliveryStatus.DELIVERED, totalQty = 15, totalAmount = 0L, createdAt = 2L, updatedAt = 2L))
        val day2Item = db.deliveryItemDao().insert(DeliveryItemEntity(deliveryId = day2Delivery, productId = productId, qty = 15, unitPrice = 0L, amount = 0L, createdAt = 2L))
        val day3Delivery = db.deliveryDao().insert(DeliveryEntity(customerId = customerId, deliveryDate = 3L, status = DeliveryStatus.DELIVERED, totalQty = 10, totalAmount = 0L, createdAt = 3L, updatedAt = 3L))
        db.deliveryItemDao().insert(DeliveryItemEntity(deliveryId = day3Delivery, productId = productId, qty = 10, unitPrice = 0L, amount = 0L, createdAt = 3L))
        suspend fun closing(date: Long): Int = db.stockBatchDao().initialStockBefore(productId, date) + db.stockBatchDao().stockInOn(productId, date) - db.deliveryItemDao().deliveredBefore(productId, date) - db.deliveryItemDao().deliveredOn(productId, date)
        assertEquals(50, closing(1L))
        assertEquals(55, closing(2L))
        assertEquals(45, closing(3L))
        db.deliveryDao().update(db.deliveryDao().get(day3Delivery)!!.copy(status = DeliveryStatus.PENDING))
        assertEquals(55, closing(3L))
        db.deliveryItemDao().update(db.deliveryItemDao().forDelivery(day2Delivery).single().copy(qty = 12))
        db.deliveryDao().update(db.deliveryDao().get(day2Delivery)!!.copy(totalQty = 12))
        db.deliveryDao().update(db.deliveryDao().get(day3Delivery)!!.copy(status = DeliveryStatus.DELIVERED))
        assertEquals(58, closing(2L))
        assertEquals(48, closing(3L))
        db.stockBatchDao().update(db.stockBatchDao().allForProduct(productId).first().copy(remainingQty = 28))
        assertEquals(48, db.stockBatchDao().total(productId))
        db.customerDao().update(db.customerDao().get(customerId)!!.copy(isActive = false))
        assertEquals(48, closing(3L))
        assertEquals(70, db.stockBatchDao().initialStockBefore(productId, 3L) + db.stockBatchDao().stockInOn(productId, 3L))
    }
}
