package com.dominik.bluepuls.domain

/**
 * Čisti domenski model profila korisnika (neovisan o Firestoreu).
 */
data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val favoritePlayer: String,
    val votesCast: Int,
    val photosUploaded: Int,
    val createdAtMillis: Long?
)
