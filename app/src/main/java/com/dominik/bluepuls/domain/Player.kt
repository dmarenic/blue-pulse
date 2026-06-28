package com.dominik.bluepuls.domain

import java.time.Year

/**
 * Domenski model igrača (neovisan o izvoru podataka - API ili seed).
 */
data class Player(
    val id: String,
    val name: String,
    val number: Int?,
    val position: String,
    val nationality: String?,
    val dateOfBirth: String?,   // npr. "2003-03-26"
    val photoUrl: String?,
    val team: String? = null    // klub igrača (za filtriranje - smije biti samo Dinamo Zagreb)
) {
    /** Prezime (zadnja riječ imena) - koristi se za sortiranje. */
    val lastName: String
        get() = name.trim().substringAfterLast(' ', missingDelimiterValue = name)

    /** Godine izračunate iz godine rođenja (null ako nije poznato). */
    val age: Int?
        get() = dateOfBirth?.take(4)?.toIntOrNull()?.let { birthYear ->
            Year.now().value - birthYear
        }
}
