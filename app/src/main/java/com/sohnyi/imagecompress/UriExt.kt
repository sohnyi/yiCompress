package com.sohnyi.imagecompress

import android.content.Context
import android.net.Uri
import android.os.FileUtils
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import java.io.File

/**
 * Uri 拓展
 *
 */

/**
 * Uri 转 File
 *
 */
fun Uri.toFile(context: Context): File? {
    Log.d("UriExt", "toFile: uri scheme: $scheme")
    return when (scheme) {
        "file" -> this.toFile()
        "document",
        "content" -> {
            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
            val cursor = context.contentResolver.query(this, filePathColumn, null, null, null)
            try {
                cursor?.moveToFirst()
                val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                columnIndex?.let {
                    cursor.getString(it)
                }?.let {
                    File(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                cursor?.close()
            }
        }
        else -> null
    }
}