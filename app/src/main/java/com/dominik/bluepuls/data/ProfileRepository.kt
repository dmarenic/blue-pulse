package com.dominik.bluepuls.data

import android.net.Uri
import com.dominik.bluepuls.core.Constants
import com.dominik.bluepuls.domain.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Repozitorij za korisnički profil (Firestore "users").
 * Avatar se uploada preko imgbb-a ([ImageUploader]); ovdje se sprema samo URL.
 * Sve operacije vraćaju [Result] pa nijedan poziv ne ruši aplikaciju.
 */
class ProfileRepository(
    private val firestore: FirebaseFirestore,
    private val imageUploader: ImageUploader
) {
    private fun usersCollection() = firestore.collection(Constants.COLLECTION_USERS)

    suspend fun ensureUserDocument(uid: String, email: String, displayName: String = ""): Result<Unit> {
        return try {
            val docRef = usersCollection().document(uid)
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val data = mapOf(
                    "uid" to uid,
                    "email" to email,
                    "displayName" to displayName,
                    "avatarUrl" to "",
                    "favoritePlayer" to "",
                    "votesCast" to 0L,
                    "photosUploaded" to 0L,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(data).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Best-effort: odražava lokalni glas na profilu (omiljeni igrač + brojač). */
    suspend fun setFavoritePlayer(uid: String, playerName: String?): Result<Unit> {
        return try {
            usersCollection().document(uid).set(
                mapOf(
                    "favoritePlayer" to (playerName ?: ""),
                    "votesCast" to if (playerName != null) 1L else 0L
                ),
                SetOptions.merge()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDisplayName(uid: String, displayName: String): Result<Unit> {
        return try {
            usersCollection().document(uid)
                .set(mapOf("displayName" to displayName.trim()), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Uploada profilnu sliku na imgbb i sprema URL u profil; vraća URL. */
    suspend fun uploadAvatar(uid: String, imageUri: Uri): Result<String> {
        return imageUploader.upload(imageUri).mapCatching { url ->
            usersCollection().document(uid)
                .set(mapOf("avatarUrl" to url), SetOptions.merge())
                .await()
            url
        }
    }

    suspend fun getUserProfile(uid: String): Result<UserProfile> {
        return try {
            val snapshot = usersCollection().document(uid).get().await()
            val dto = snapshot.toObject(UserDto::class.java)
                ?: return Result.failure(IllegalStateException("Profil nije pronađen."))
            Result.success(dto.toDomain(snapshot.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
