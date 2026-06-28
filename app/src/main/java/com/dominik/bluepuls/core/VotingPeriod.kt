package com.dominik.bluepuls.core

import java.time.YearMonth

/**
 * Period glasanja = jedan kalendarski mjesec (npr. "2026-06").
 * Igrač mjeseca se računa po periodu; svaki mjesec ima svog pobjednika.
 */
object VotingPeriod {

    /** Trenutni period "yyyy-MM". */
    fun current(): String = YearMonth.now().toString()

    /** Prethodni mjesec "yyyy-MM" (za zaključivanje rezultata). */
    fun previous(): String = YearMonth.now().minusMonths(1).toString()

    fun year(period: String): Int = parse(period)?.year ?: 0
    fun month(period: String): Int = parse(period)?.monthValue ?: 0

    /** "2026-06" -> "Lipanj 2026." */
    fun label(period: String): String {
        val ym = parse(period) ?: return period
        return "${CROATIAN_MONTHS[ym.monthValue - 1]} ${ym.year}."
    }

    private fun parse(period: String): YearMonth? = try {
        YearMonth.parse(period)
    } catch (e: Exception) {
        null
    }

    private val CROATIAN_MONTHS = listOf(
        "Siječanj", "Veljača", "Ožujak", "Travanj", "Svibanj", "Lipanj",
        "Srpanj", "Kolovoz", "Rujan", "Listopad", "Studeni", "Prosinac"
    )
}
