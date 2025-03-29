package org.kotfind.android_course

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore

import android.util.Log

fun writeBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
    fileName: String,
) {
    try {
        val contentValues = ContentValues().apply{
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val resolver = context.contentResolver

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        var outputStream = resolver.openOutputStream(uri!!)!!

        outputStream.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    } catch (e: Exception) {
        Log.e("writeBitmapToGallery", "failed to write to gallery", e)
    }
}
