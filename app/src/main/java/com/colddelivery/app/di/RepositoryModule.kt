package com.colddelivery.app.di

import com.colddelivery.app.core.fifo.FifoStockManager
import com.colddelivery.app.data.repository.DeliveryRepository
import com.colddelivery.app.data.repository.RoomDeliveryRepository
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.content.Context
import com.colddelivery.app.data.preferences.PreferencesStore
import dagger.hilt.android.qualifiers.ApplicationContext

@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindDeliveryRepository(repository: RoomDeliveryRepository): DeliveryRepository
    companion object { @Provides @Singleton fun fifoManager() = FifoStockManager(); @Provides @Singleton fun preferences(@ApplicationContext context: Context) = PreferencesStore(context) }
}
