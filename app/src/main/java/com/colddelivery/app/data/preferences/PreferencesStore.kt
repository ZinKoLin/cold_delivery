package com.colddelivery.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

private val Context.preferencesDataStore by preferencesDataStore("cold_delivery_preferences")
class PreferencesStore(private val context: Context) {
    data class Snapshot(val language: String, val rememberMe: Boolean, val lowStockThreshold: Int)
    val file get() = context.preferencesDataStoreFile("cold_delivery_preferences")
    val rememberMe = context.preferencesDataStore.data.map { it[booleanPreferencesKey("remember_me")] ?: false }
    val language = context.preferencesDataStore.data.map { it[stringPreferencesKey("language")] ?: "en" }
    val lowStockThreshold = context.preferencesDataStore.data.map { it[androidx.datastore.preferences.core.intPreferencesKey("low_stock_threshold")] ?: 10 }
    suspend fun setLanguage(value: String) { context.preferencesDataStore.edit { it[stringPreferencesKey("language")] = value } }
    suspend fun setRememberMe(value: Boolean) { context.preferencesDataStore.edit { it[booleanPreferencesKey("remember_me")] = value } }
    suspend fun setLowStockThreshold(value: Int) { context.preferencesDataStore.edit { it[androidx.datastore.preferences.core.intPreferencesKey("low_stock_threshold")] = value } }
    suspend fun exportSnapshot(): Snapshot { val values = context.preferencesDataStore.data.first(); return Snapshot(values[stringPreferencesKey("language")] ?: "en", values[booleanPreferencesKey("remember_me")] ?: false, values[androidx.datastore.preferences.core.intPreferencesKey("low_stock_threshold")] ?: 10) }
    suspend fun importSnapshot(snapshot: Snapshot) { context.preferencesDataStore.edit { it[stringPreferencesKey("language")] = snapshot.language; it[booleanPreferencesKey("remember_me")] = snapshot.rememberMe; it[androidx.datastore.preferences.core.intPreferencesKey("low_stock_threshold")] = snapshot.lowStockThreshold } }
}

fun readSavedLanguage(context: Context): String = runBlocking { context.preferencesDataStore.data.first()[stringPreferencesKey("language")] ?: "en" }
