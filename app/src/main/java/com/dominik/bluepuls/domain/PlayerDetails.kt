package com.dominik.bluepuls.domain

import java.time.Year

/**
 * Detaljni podaci o igraču (TheSportsDB lookupplayer).
 */
data class PlayerDetails(
    val id: String,
    val name: String,
    val nationality: String?,
    val position: String?,
    val dateOfBirth: String?,
    val height: String?,
    val weight: String?,
    val description: String?,
    val photoUrl: String?,
    val team: String?,
    val number: Int?
) {
    val age: Int?
        get() = dateOfBirth?.take(4)?.toIntOrNull()?.let { Year.now().value - it }
}
