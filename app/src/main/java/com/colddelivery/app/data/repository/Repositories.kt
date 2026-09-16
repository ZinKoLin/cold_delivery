package com.colddelivery.app.data.repository

import com.colddelivery.app.data.local.dao.*
import com.colddelivery.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

interface ProductRepository { fun observeActive(): Flow<List<ProductEntity>>; suspend fun get(id: Long): ProductEntity?; suspend fun add(product: ProductEntity): Long }
interface CustomerRepository { fun observeActive(): Flow<List<CustomerEntity>>; suspend fun get(id: Long): CustomerEntity?; suspend fun add(customer: CustomerEntity): Long }
interface StockRepository { suspend fun availableFifo(productId: Long): List<StockBatchEntity>; suspend fun total(productId: Long): Int; suspend fun add(batch: StockBatchEntity): Long }
interface DailyPriceRepository { fun observeForDate(date: Long): Flow<List<DailyPriceEntity>>; suspend fun get(productId: Long, date: Long): DailyPriceEntity?; suspend fun save(price: DailyPriceEntity) }

class DaoProductRepository(private val dao: ProductDao) : ProductRepository { override fun observeActive() = dao.observeActive(); override suspend fun get(id: Long) = dao.get(id); override suspend fun add(product: ProductEntity) = dao.insert(product) }
class DaoCustomerRepository(private val dao: CustomerDao) : CustomerRepository { override fun observeActive() = dao.observeActive(); override suspend fun get(id: Long) = dao.get(id); override suspend fun add(customer: CustomerEntity) = dao.insert(customer) }
class DaoStockRepository(private val dao: StockBatchDao) : StockRepository { override suspend fun availableFifo(productId: Long) = dao.availableFifo(productId); override suspend fun total(productId: Long) = dao.total(productId); override suspend fun add(batch: StockBatchEntity) = dao.insert(batch) }
class DaoDailyPriceRepository(private val dao: DailyPriceDao) : DailyPriceRepository { override fun observeForDate(date: Long) = dao.observeForDate(date); override suspend fun get(productId: Long, date: Long) = dao.get(productId, date); override suspend fun save(price: DailyPriceEntity) = dao.upsert(price) }
