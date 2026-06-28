package com.dominik.bluepuls.data

import com.dominik.bluepuls.domain.Photo
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Firestore reprezentacija metapodataka fotografije (kolekcija "photos").
 * Sama slika je hostana na imgbb-u; ovdje je samo javni URL + metapodaci + lajkovi.
 */
data class PhotoDto(
    val id: String = "",
    val url: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val caption: String = "",
    val likedBy: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

fun PhotoDto.toDomain(currentUid: String?): Photo = Photo(
    id = id,
    url = url,
    caption = caption,
    uploaderName = userName,
    uploaderEmail = userEmail,
    createdAtMillis = createdAt?.toDate()?.time,
    likeCount = likedBy.size,
    isLikedByMe = currentUid != null && currentUid in likedBy,
    isMine = currentUid != null && userId == currentUid
)
