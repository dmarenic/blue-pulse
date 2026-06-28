package com.dominik.bluepuls.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * imgbb besplatni image-hosting API (alternativa Firebase Storageu, radi na Spark planu).
 * Slika se šalje kao base64 string; odgovor sadrži javni URL.
 */
interface ImageUploadApi {

    @FormUrlEncoded
    @POST("upload")
    suspend fun upload(
        @Query("key") apiKey: String,
        @Field("image") base64Image: String
    ): ImgbbResponse
}

data class ImgbbResponse(
    @SerializedName("data") val data: ImgbbData?,
    @SerializedName("success") val success: Boolean = false
)

data class ImgbbData(
    @SerializedName("url") val url: String?,
    @SerializedName("display_url") val displayUrl: String?,
    @SerializedName("delete_url") val deleteUrl: String?
)
