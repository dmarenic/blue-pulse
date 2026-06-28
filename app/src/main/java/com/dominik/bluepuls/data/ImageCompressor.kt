package com.dominik.bluepuls.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

/**
 * Smanjuje i komprimira sliku prije uploada.
 *
 * Zašto: korisničke fotografije znaju biti 5-15 MB. Bez ovoga bismo riskirali
 * OutOfMemoryError pri dekodiranju i prelazak Storage limita (5 MB).
 * Downsampling preko inSampleSize dekodira odmah u manjoj rezoluciji (memory-safe).
 */
object ImageCompressor {

    private const val MAX_DIMENSION = 1280
    private const val JPEG_QUALITY = 80

    fun compress(context: Context, uri: Uri): ByteArray {
        val resolver = context.contentResolver

        // 1. Izmjeri dimenzije bez učitavanja cijele slike u memoriju.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        // 2. Dekodiraj u smanjenoj rezoluciji.
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
        }
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IllegalStateException("Sliku nije moguće pročitati.")

        // 3. Komprimiraj u JPEG.
        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            bitmap.recycle()
            out.toByteArray()
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        if (width <= 0 || height <= 0) return 1
        var sampleSize = 1
        while (width / sampleSize > MAX_DIMENSION || height / sampleSize > MAX_DIMENSION) {
            sampleSize *= 2
        }
        return sampleSize
    }
}
