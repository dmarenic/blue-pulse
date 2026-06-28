package com.dominik.bluepuls.core

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Centralizirano parsiranje i prikaz datuma/vremena utakmica.
 *
 * TheSportsDB polje `strTime` ("16:45:00") je u **UTC-u**, a `dateEvent`
 * ("2026-05-23") je kalendarski datum. Sve konverzije idu iz UTC-a u lokalnu
 * zonu uređaja kako podsjetnici, odbrojavanje i unos u kalendar ne bi bili
 * pomaknuti za sat/dva ovisno o vremenskoj zoni korisnika.
 *
 * Jedino mjesto u aplikaciji gdje se parsira vrijeme utakmice (nema dupliciranja).
 */
object MatchDateTime {

    private const val DEFAULT_TIME = "18:00"
    private val DISPLAY_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.")
    private val DISPLAY_DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. 'u' HH:mm")

    /** "HH:mm" iz API vremena, ili null ako vrijeme nije poznato/valjano. */
    private fun normalizedTime(time: String?): String? =
        time?.takeIf { it.length >= 5 }?.substring(0, 5)

    /** UTC LocalDateTime iz API datuma + vremena (vrijeme se tretira kao UTC). */
    private fun parseUtc(date: String, time: String?): LocalDateTime? {
        return try {
            LocalDateTime.parse("${date}T${normalizedTime(time) ?: DEFAULT_TIME}")
        } catch (e: Exception) {
            null
        }
    }

    /** Vrijeme početka u epoch milisekundama (UTC -> instant), ili null ako se ne može parsirati. */
    fun kickoffMillis(date: String, time: String?): Long? =
        parseUtc(date, time)?.toInstant(ZoneOffset.UTC)?.toEpochMilli()

    /**
     * Čitljiv prikaz u **lokalnoj** zoni uređaja: npr. "02.07.2026. u 18:45".
     * Ako vrijeme nije poznato, prikazuje samo datum (bez zonskog pomaka).
     */
    fun formatted(date: String, time: String?): String {
        if (normalizedTime(time) == null) {
            return try {
                LocalDate.parse(date).format(DISPLAY_DATE)
            } catch (e: Exception) {
                date
            }
        }
        val utc = parseUtc(date, time) ?: return date
        return utc.atZone(ZoneOffset.UTC)
            .withZoneSameInstant(ZoneId.systemDefault())
            .format(DISPLAY_DATE_TIME)
    }
}
