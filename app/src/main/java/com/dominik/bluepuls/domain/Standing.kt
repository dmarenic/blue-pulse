package com.dominik.bluepuls.domain

/**
 * Jedan red ljestvice lige.
 */
data class Standing(
    val rank: Int,
    val teamId: String,
    val teamName: String,
    val badgeUrl: String?,
    val played: Int,
    val win: Int,
    val draw: Int,
    val loss: Int,
    val goalDifference: Int,
    val points: Int,
    val form: String?
)
