/*
 * Copyright (c) 2026 Nishant Mishra
 *
 * This file is part of Tomato - a minimalist pomodoro timer for Android.
 *
 * Tomato is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tomato is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tomato.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.time.Clock

actual data class FileLocator(val uri: Uri?) {
    actual constructor() : this(null)

    actual val path: String?
        get() = uri?.path

    actual val isNull: Boolean
        get() = uri == null
}

actual fun FileLocator.fromPath(path: String): FileLocator {
    val uri = if (path.startsWith("content://") || path.startsWith("file://")) {
        Uri.parse(path)
    } else {
        Uri.fromFile(File(path))
    }
    return FileLocator(uri)
}

class AndroidBackupRestoreManager(
    private val database: AppDatabase,
    private val systemDao: SystemDao,
    private val context: Context
) : BackupRestoreManager {
    override suspend fun performBackup(directoryLocator: FileLocator) {
        withContext(Dispatchers.IO) {
            systemDao.checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))

            val dbName = "app_database"
            val dbFile = context.getDatabasePath(dbName)

            val uri = directoryLocator.uri ?: return@withContext

            if (uri.scheme == "file") {
                // 文件路径模式：直接复制到目标目录
                val dir = File(uri.path!!)
                if (!dir.exists()) dir.mkdirs()
                val backupFile = File(dir, "tomato-backup-${Clock.System.now()}.db")
                FileInputStream(dbFile).use { input ->
                    FileOutputStream(backupFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                // SAF 模式：通过 ContentResolver
                val documentId = DocumentsContract.getTreeDocumentId(uri)
                val parentDocumentUri =
                    DocumentsContract.buildDocumentUriUsingTree(uri, documentId)

                val fileUri = DocumentsContract.createDocument(
                    context.contentResolver,
                    parentDocumentUri,
                    "application/octet-stream",
                    "tomato-backup-${Clock.System.now()}.db"
                )

                fileUri?.let {
                    context.contentResolver.openOutputStream(it)?.use { output ->
                        FileInputStream(dbFile).use { input ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }

    override suspend fun performRestore(fileLocator: FileLocator) {
        if (fileLocator.isNull) return
        withContext(Dispatchers.IO) {
            database.close()

            val dbName = "app_database"
            val dbFile = context.getDatabasePath(dbName)

            if (!dbFile.parentFile!!.exists()) dbFile.parentFile!!.mkdirs()

            File("${dbFile.path}-wal").delete()
            File("${dbFile.path}-shm").delete()

            val uri = fileLocator.uri!!

            if (uri.scheme == "file") {
                // 文件路径模式：直接复制
                val sourceFile = File(uri.path!!)
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                // SAF 模式：通过 ContentResolver
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    override fun restartApp() {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component

        val mainIntent = Intent.makeRestartActivityTask(componentName)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}