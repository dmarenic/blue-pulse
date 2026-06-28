package com.dominik.bluepuls.domain

/**
 * Domenski model fotografije u galeriji navijača.
 */
data class Photo(
    val id: String,
    val url: String,
    val caption: String,
    val uploaderName: String,
    val uploaderEmail: String,
    val createdAtMillis: Long?,
    val likeCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val isMine: Boolean = false
) {
    /** Ime autora za prikaz; ako ime nije poznato (starije fotografije), vraća e-mail. */
    val uploaderLabel: String get() = uploaderName.ifBlank { uploaderEmail }
}
