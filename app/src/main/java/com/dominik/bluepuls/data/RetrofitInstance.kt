package com.dominik.bluepuls.data

import com.dominik.bluepuls.BuildConfig
import com.dominik.bluepuls.core.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton koji konfigurira Retrofit klijent za TheSportsDB.
 *
 * TheSportsDB V1 stavlja API ključ u PUTANJU (.../json/{key}/...), pa ga
 * lijepimo na bazni URL. Ključ dolazi iz BuildConfig.SPORTSDB_API_KEY
 * (vrijednost je u local.properties - nije u kodu ni u gitu).
 */
object RetrofitInstance {

    private val baseUrl: String =
        Constants.SPORTSDB_BASE_URL + BuildConfig.SPORTSDB_API_KEY + "/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: SportsDbApi by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SportsDbApi::class.java)
    }

    // imgbb image-hosting (zamjena Firebase Storagea).
    val imageUploadApi: ImageUploadApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImageUploadApi::class.java)
    }
}
