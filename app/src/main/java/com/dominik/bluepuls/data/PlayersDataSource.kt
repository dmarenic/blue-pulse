package com.dominik.bluepuls.data

import com.dominik.bluepuls.domain.Player

/**
 * Lokalni izvor igrača GNK Dinamo Zagreb.
 *
 * - [excludedPlayerIds]: igrači koje TheSportsDB i dalje vodi kao "Dinamo Zagreb",
 *   ali NISU dio prve momčadi - izbacuju se i iz API odgovora i iz cachea.
 * - [firstTeamExtras]: aktualni igrači koje besplatni ključ ne vraća - ručno dodani
 *   (provjereni preko TheSportsDB-a kao trenutni članovi Dinamo Zagreba).
 * - [dinamoSquad]: puni fallback popis kad API uopće ne radi.
 */
object PlayersDataSource {

    private const val DINAMO = "Dinamo Zagreb"

    private const val CUTOUT_BELJO =
        "https://r2.thesportsdb.com/images/media/player/cutout/e1hog31678362200.png"
    private const val CUTOUT_MCKENNA =
        "https://r2.thesportsdb.com/images/media/player/cutout/znbep11707938012.png"
    private const val CUTOUT_HOXHA =
        "https://r2.thesportsdb.com/images/media/player/cutout/z2979r1726001599.png"
    private const val CUTOUT_VIDOVIC =
        "https://r2.thesportsdb.com/images/media/player/cutout/d8qdq01741449778.png"
    private const val CUTOUT_PIERRE =
        "https://r2.thesportsdb.com/images/media/player/cutout/isvd271678742586.png"

    /** Igrači koje treba izbaciti iako ih API vodi kao Dinamo (napustili klub / nisu prva momčad). */
    val excludedPlayerIds: Set<String> = setOf(
        "34221323", // Antonio Rajić
        "34221331", // Bartol Barišić
        "34221321", // Bohdan Mykhailichenko
        "34221290", // Alessio Tonon - napustio klub
        "34221295", // Anes Krdžalić - napustio klub
        "34177775"  // Arijan Ademi - napustio klub
    )

    /** Ručno dodani aktualni igrači prve momčadi (nadopuna API liste). */
    val firstTeamExtras: List<Player> = listOf(
        Player("34203396", "Dion Beljo", null, "Centre-Forward", "Croatia", "2002-03-01", CUTOUT_BELJO, DINAMO),
        Player("34194220", "Raúl Torrente", null, "Centre-Back", "Spain", "2001-03-01", null, DINAMO),
        Player("manual-hoxha", "Arbër Hoxha", 20, "Attacker", "Albania", "1998-10-06", CUTOUT_HOXHA, DINAMO),
        Player("34156666", "Scott McKenna", null, "Centre-Back", "Scotland", "1996-11-12", CUTOUT_MCKENNA, DINAMO),
        Player("34290353", "Monsef Bakrar", null, "Forward", "Algeria", "2001-01-13", null, DINAMO),
        Player("34200294", "Gabriel Vidović", null, "Left Wing", "Croatia", "2003-12-01", CUTOUT_VIDOVIC, DINAMO),
        Player("34248009", "Moris Valinčić", null, "Defender", "Croatia", "2002-11-17", null, DINAMO),
        Player("34177834", "Josip Mišić", null, "Midfielder", "Croatia", "1994-06-28", null, DINAMO),
        Player("34221282", "Ivan Nevistić", null, "Goalkeeper", "Croatia", "1998-07-31", null, DINAMO),
        Player("34236383", "Niko Galešić", null, "Centre-Back", "Croatia", "2001-03-26", null, DINAMO),
        Player("34163682", "Ronaël Pierre-Gabriel", null, "Defender", "France", "1998-06-13", CUTOUT_PIERRE, DINAMO),
        Player("34221317", "Luka Stojković", null, "Left Wing", "Croatia", "2003-10-28", null, DINAMO)
    )

    private val baseSeed: List<Player> = listOf(
        Player("seed-zagorac", "Danijel Zagorac", 1, "Goalkeeper", "Croatia", "1987-02-14", null, DINAMO),
        Player("seed-goda", "Bruno Goda", null, "Defender", "Croatia", null, null, DINAMO)
    )

    /** Puni fallback popis (kad API uopće ne radi) = osnovni seed + ručno dodani, bez duplikata. */
    val dinamoSquad: List<Player> = (baseSeed + firstTeamExtras)
        .distinctBy { it.lastName.lowercase() }
}
