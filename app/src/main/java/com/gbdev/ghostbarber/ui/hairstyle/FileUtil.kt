package com.gbdev.ghostbarber.ui.hairstyle

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

object FileUtil {
    fun getPath(context: Context, uri: Uri): String? {
        when (uri.scheme) {
            "content" -> {
                val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val index: Int = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        return it.getString(index)
                    }
                }
            }
            "file" -> return uri.path
        }
        return null
    }
}
