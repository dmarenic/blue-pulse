package com.dominik.bluepuls.data

import com.dominik.bluepuls.domain.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Firestore reprezentacija dokumenta u kolekciji "users".
 * Sva polja imaju default vrijednosti -> Firestore može deserijalizirati
 * preko praznog konstruktora bez pada.
 */
data class UserDto(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val favoritePlayer: String = "",
    val votesCast: Long = 0,
    val photosUploaded: Long = 0,
    @ServerTimestamp val createdAt: Timestamp? = null
)

fun UserDto.toDomain(documentId: String): UserProfile = UserProfile(
    uid = uid.ifEmpty { documentId },
    email = email,
    displayName = displayName,
    avatarUrl = avatarUrl.ifBlank { null },
    favoritePlayer = favoritePlayer,
    votesCast = votesCast.toInt(),
    photosUploaded = photosUploaded.toInt(),
    createdAtMillis = createdAt?.toDate()?.time
)
