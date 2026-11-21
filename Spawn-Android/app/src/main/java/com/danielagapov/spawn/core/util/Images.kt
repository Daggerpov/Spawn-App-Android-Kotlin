package com.danielagapov.spawn.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

/** A picked photo as a JPEG at most 1024px on its long edge, like iOS's resized upload. */
fun compressPhoto(context: Context, uri: Uri): ByteArray {
    val src = context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) } ?: error("Couldn't read that photo")
    val scale = 1024f / maxOf(src.width, src.height)
    val bmp = if (scale < 1f) Bitmap.createScaledBitmap(src, (src.width * scale).toInt(), (src.height * scale).toInt(), true) else src
    return ByteArrayOutputStream().also { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }.toByteArray()
}
