package com.colddelivery.app.di

import android.content.Context
import androidx.room.Room
import com.colddelivery.app.data.local.database.ColdDeliveryDatabase
import com.colddelivery.app.data.local.database.MIGRATION_1_2
import com.colddelivery.app.data.local.database.MIGRATION_2_3
import com.colddelivery.app.data.local.database.DemoSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): ColdDeliveryDatabase {
        val database = Room.databaseBuilder(context, ColdDeliveryDatabase::class.java, "cold_delivery.db").addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
        CoroutineScope(Dispatchers.IO).launch { DemoSeeder.seed(database) }
        return database
    }
}
