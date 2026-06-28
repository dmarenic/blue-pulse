package com.dominik.bluepuls.domain

/**
 * Osnovni podaci o klubu (grb, stadion) dohvaćeni s TheSportsDB.
 */
data class ClubInfo(
    val name: String,
    val badgeUrl: String?,
    val stadium: String?,
    val location: String?,
    val capacity: String?
)
