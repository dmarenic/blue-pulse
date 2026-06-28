package com.dominik.bluepuls.data

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.dominik.bluepuls.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Zajednički servis za upload slika preko imgbb-a (galerija + avatar).
 * Kompresira sliku, kodira u base64 i šalje na imgbb; vraća javni URL.
 * Zamjenjuje Firebase Storage (radi na besplatnom Spark planu).
 */
class ImageUploader(
    private val context: Context,
    private val api: ImageUploadApi
) {
    suspend fun upload(imageUri: Uri): Result<String> {
        return try {
            if (BuildConfig.IMGBB_API_KEY.isBlank()) {
                return Result.failure(IllegalStateException("imgbb ključ nije postavljen (local.properties)."))
            }
            val base64 = withContext(Dispatchers.IO) {
                val bytes = ImageCompressor.compress(context, imageUri)
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
            val response = api.upload(BuildConfig.IMGBB_API_KEY, base64)
            val url = response.data?.displayUrl ?: response.data?.url
            if (response.success && !url.isNullOrBlank()) {
                Result.success(url)
            } else {
                Result.failure(IllegalStateException("Upload slike nije uspio."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}