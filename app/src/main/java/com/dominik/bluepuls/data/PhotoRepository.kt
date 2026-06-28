package com.dominik.bluepuls.data

import com.dominik.bluepuls.core.Constants
import com.dominik.bluepuls.domain.Photo
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Repozitorij za galeriju: slika ide na imgbb (besplatni host), metapodaci u Firestore.
 * Sve operacije vraćaju [Result] - nijedan poziv ne ruši aplikaciju.
 */
class PhotoRepository(
    private val firestore: FirebaseFirestore,
    private val imageUploader: ImageUploader
) {
    private fun photosCollection() = firestore.collection(Constants.COLLECTION_PHOTOS)
    private fun usersCollection() = firestore.collection(Constants.COLLECTION_USERS)

    suspend fun uploadPhoto(
        userId: String,
        userEmail: String,
        userName: String,
        imageUri: android.net.Uri,
        caption: String
    ): Result<Unit> {
        return imageUploader.upload(imageUri).mapCatching { url ->
            val docRef = photosCollection().document()
            docRef.set(
                mapOf(
                    "id" to docRef.id,
                    "url" to url,
                    "userId" to userId,
                    "userEmail" to userEmail,
                    "userName" to userName,
                    "caption" to caption.trim(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            usersCollection().document(userId)
                .update("photosUploaded", FieldValue.increment(1))
                .await()
            Unit
        }
    }

    suspend fun getPhotos(currentUid: String?): Result<List<Photo>> {
        return try {
            val snapshot = photosCollection()
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val photos = snapshot.toObjects(PhotoDto::class.java).map { it.toDomain(currentUid) }
            Result.success(photos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Briše vlastitu fotografiju (metapodaci iz Firestorea) i smanjuje brojač. */
    suspend fun deletePhoto(photoId: String, uid: String): Result<Unit> {
        return try {
            photosCollection().document(photoId).delete().await()
            usersCollection().document(uid)
                .update("photosUploaded", FieldValue.increment(-1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Lajk / odlajk fotografije (dodaje/uklanja uid iz polja likedBy). */
    suspend fun toggleLike(photoId: String, uid: String, like: Boolean): Result<Unit> {
        return try {
            val change = if (like) FieldValue.arrayUnion(uid) else FieldValue.arrayRemove(uid)
            photosCollection().document(photoId).update("likedBy", change).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
