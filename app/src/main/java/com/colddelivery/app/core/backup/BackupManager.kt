package com.colddelivery.app.core.backup

import android.content.Context
import android.net.Uri
import androidx.room.RoomDatabase
import com.colddelivery.app.data.preferences.PreferencesStore
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context, private val database: RoomDatabase, private val preferences: PreferencesStore = PreferencesStore(context)) {
    fun create(uri: Uri): Result<Unit> = runCatching {
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
        val dbFile = context.getDatabasePath("cold_delivery.db")
        context.contentResolver.openOutputStream(uri)?.use { output -> ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("cold_delivery.db")); dbFile.inputStream().use { it.copyTo(zip) }; zip.closeEntry()
            val snapshot = runBlocking { preferences.exportSnapshot() }
            zip.putNextEntry(ZipEntry("preferences.txt")); zip.write("language=${snapshot.language}\nrememberMe=${snapshot.rememberMe}\nlowStockThreshold=${snapshot.lowStockThreshold}\n".toByteArray()); zip.closeEntry()
            zip.putNextEntry(ZipEntry("metadata.txt")); zip.write("app=com.colddelivery.app\ndatabaseVersion=2\n".toByteArray()); zip.closeEntry()
        } } ?: error("Unable to open backup destination")
    }
    fun restore(uri: Uri): Result<Unit> = runCatching {
        val temp = File(context.cacheDir, "restore-${System.currentTimeMillis()}.zip")
        context.contentResolver.openInputStream(uri)?.use { input -> temp.outputStream().use { output -> input.copyTo(output) } } ?: error("Unable to read backup")
        val entries = mutableMapOf<String, ByteArray>(); ZipInputStream(temp.inputStream()).use { zip -> var entry = zip.nextEntry; while (entry != null) { entries[entry.name] = zip.readBytes(); entry = zip.nextEntry } }
        require(entries["metadata.txt"]?.decodeToString()?.contains("databaseVersion=2") == true) { "Unsupported backup version" }
        require(entries.containsKey("cold_delivery.db")) { "Backup database is missing" }
        val dbFile = context.getDatabasePath("cold_delivery.db"); File(dbFile.parentFile, "cold_delivery_safety_${System.currentTimeMillis()}.db").let { dbFile.copyTo(it, overwrite = true) }
        database.close(); dbFile.writeBytes(entries.getValue("cold_delivery.db"))
        entries["preferences.txt"]?.decodeToString()?.lineSequence()?.filter { it.contains('=') }?.associate { it.substringBefore('=') to it.substringAfter('=') }?.let { values ->
            val snapshot = PreferencesStore.Snapshot(values["language"] ?: "en", values["rememberMe"]?.toBoolean() ?: false, values["lowStockThreshold"]?.toIntOrNull() ?: 10)
            runBlocking { preferences.importSnapshot(snapshot) }
        }
        temp.delete()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
