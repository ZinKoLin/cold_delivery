package com.colddelivery.app.ui.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colddelivery.app.data.local.database.ColdDeliveryDatabase
import com.colddelivery.app.data.local.entity.*
import com.colddelivery.app.data.preferences.PreferencesStore
import com.colddelivery.app.data.repository.DeliveryRepository
import com.colddelivery.app.core.history.HistoricalStockBalance
import com.colddelivery.app.core.history.requireHistoricalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.room.withTransaction
import javax.inject.Inject

@HiltViewModel
class FeatureViewModel @Inject constructor(private val db: ColdDeliveryDatabase, private val repository: DeliveryRepository, private val preferences: PreferencesStore) : ViewModel() {
    fun databaseForBackup() = db
    fun preferencesForBackup() = preferences
    val products = repository.observeProducts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val customers = repository.observeCustomers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val deliveries = repository.observeHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val totalStock = repository.observeTotalStock().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val stockInTickets = db.stockInTicketDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val language = preferences.language.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "en")
    val lowStockThreshold = preferences.lowStockThreshold.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 10)
    val today: Long get() = LocalDate.now().toEpochDay()
    fun prices(date: Long = today): Flow<List<DailyPriceEntity>> = db.dailyPriceDao().observeForDate(date)
    fun stockTotal(productId: Long): Int = kotlinx.coroutines.runBlocking { db.stockBatchDao().total(productId) }
    suspend fun stockBatches(productId: Long) = db.stockBatchDao().allForProduct(productId)
    suspend fun items(deliveryId: Long) = db.deliveryItemDao().forDelivery(deliveryId)
    suspend fun delivery(deliveryId: Long) = db.deliveryDao().get(deliveryId)
    suspend fun customer(id: Long) = db.customerDao().get(id)
    fun saveLanguage(value: String) = viewModelScope.launch { preferences.setLanguage(value) }
    fun saveThreshold(value: Int) = viewModelScope.launch { preferences.setLowStockThreshold(value) }
    suspend fun addCustomer(customer: CustomerEntity) = db.customerDao().insert(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = db.customerDao().update(customer)
    suspend fun moveCustomer(customerId: Long, direction: Int) = db.withTransaction {
        val current = db.customerDao().get(customerId) ?: return@withTransaction
        val ordered = db.customerDao().orderedForDay(current.deliveryDay.name)
        val index = ordered.indexOfFirst { it.id == customerId }
        val target = index + direction
        if (index < 0 || target !in ordered.indices) return@withTransaction
        val other = ordered[target]
        db.customerDao().update(current.copy(sortOrder = other.sortOrder))
        db.customerDao().update(other.copy(sortOrder = current.sortOrder))
    }
    suspend fun addProduct(product: ProductEntity) = db.productDao().insert(product)
    suspend fun updateProduct(product: ProductEntity) = db.productDao().update(product)
    suspend fun stockIn(batch: StockBatchEntity) = db.stockBatchDao().insert(batch)
    suspend fun createStockInTicket(date: Long, items: List<Pair<Long, Int>>): Long = db.withTransaction {
        val ticketId = db.stockInTicketDao().insert(StockInTicketEntity(ticketDate = date, createdAt = System.currentTimeMillis()))
        db.stockBatchDao().insertAll(items.map { (productId, qty) -> StockBatchEntity(productId = productId, stockInDate = date, initialQty = qty, remainingQty = qty, createdAt = System.currentTimeMillis(), ticketId = ticketId) })
        ticketId
    }
    suspend fun stockInTicket(ticketId: Long) = db.stockInTicketDao().get(ticketId)
    suspend fun stockInTicketBatches(ticketId: Long) = db.stockInTicketDao().batches(ticketId)
    suspend fun savePrice(productId: Long, date: Long, price: Long) { val now = System.currentTimeMillis(); db.dailyPriceDao().upsert(DailyPriceEntity(productId = productId, priceDate = date, price = price, createdAt = now, updatedAt = now)) }
    suspend fun saveDelivery(customerId: Long, date: Long, items: List<Pair<Long, Int>>): Long = repository.saveDelivered(customerId, date, items, System.currentTimeMillis())
    suspend fun undo(deliveryId: Long) = repository.undo(deliveryId)
    suspend fun edit(deliveryId: Long, items: List<Pair<Long, Int>>) = repository.editDelivered(deliveryId, items, System.currentTimeMillis())
    suspend fun historicalStock(productId: Long, date: Long): HistoricalStockBalance {
        requireHistoricalDate(date, today)
        val opening = db.stockBatchDao().initialStockBefore(productId, date) - db.deliveryItemDao().deliveredBefore(productId, date)
        return HistoricalStockBalance(productId, opening, db.stockBatchDao().stockInOn(productId, date), db.deliveryItemDao().deliveredOn(productId, date))
    }
    suspend fun historicalMovements(productId: Long, date: Long) = Pair(db.stockBatchDao().allForProduct(productId).filter { it.stockInDate == date }, db.deliveryItemDao().movementsOn(productId, date))
}
