package com.dominik.bluepuls.core

/**
 * Odakle dolaze podaci u trostrukom fallbacku (API -> Firestore cache -> lokalni seed).
 * Koristi se za prikaz malog "izvor" badgea na ekranu (transparentnost prema korisniku).
 */
enum class DataOrigin { API, CACHE, LOCAL }

/** Podaci + informacija o tome iz kojeg su sloja dohvaćeni. */
data class Sourced<T>(
    val data: T,
    val origin: DataOrigin
)
