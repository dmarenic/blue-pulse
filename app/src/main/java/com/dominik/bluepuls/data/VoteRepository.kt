package com.dominik.bluepuls.data

import com.dominik.bluepuls.core.Constants
import com.dominik.bluepuls.core.VotingPeriod
import com.dominik.bluepuls.domain.PlayerOfMonth
import com.dominik.bluepuls.domain.VoteCount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Repozitorij za glasanje "Igrač mjeseca".
 *
 * - Glasovi: Firestore "votes", document ID = "{uid}_{period}" -> jedan glas po
 *   korisniku po mjesecu, promjenjiv do kraja mjeseca (overwrite mijenja glas).
 * - Povijest pobjednika: kolekcija player_of_the_month_history, document ID = period
 *   -> jedan pobjednik po mjesecu (ne može se prepisati).
 */
class VoteRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val playerRepository: PlayerRepository
) {
    private fun votesCollection() = firestore.collection(Constants.COLLECTION_VOTES)
    private fun historyCollection() = firestore.collection(Constants.COLLECTION_POM_HISTORY)

    /** ID igrača za kojeg je trenutni korisnik glasao u periodu (ili null). */
    suspend fun currentVote(period: String): Result<String?> {
        val uid = auth.currentUser?.uid ?: return Result.success(null)
        return try {
            val snapshot = votesCollection().document("${uid}_$period").get().await()
            Result.success(snapshot.getString("playerId"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun castVote(playerId: String, playerName: String, period: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        return try {
            votesCollection().document("${uid}_$period").set(
                mapOf(
                    "userId" to uid,
                    "playerId" to playerId,
                    "playerName" to playerName,
                    "period" to period,
                    "votedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeVote(period: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.success(Unit)
        return try {
            votesCollection().document("${uid}_$period").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Zbroj glasova po igraču za period (sortirano silazno). */
    suspend fun leaderboard(period: String): Result<List<VoteCount>> {
        return try {
            val snapshot = votesCollection().whereEqualTo("period", period).get().await()
            val counts = snapshot.documents
                .mapNotNull { doc ->
                    val pid = doc.getString("playerId") ?: return@mapNotNull null
                    val pname = doc.getString("playerName") ?: pid
                    pid to pname
                }
                .groupingBy { it }
                .eachCount()
                .map { (pair, c) -> VoteCount(pair.first, pair.second, c) }
                .sortedByDescending { it.count }
            Result.success(counts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Kronološka povijest pobjednika (najnoviji prvi). */
    suspend fun getHistory(): Result<List<PlayerOfMonth>> {
        return try {
            val snapshot = historyCollection()
                .orderBy("period", Query.Direction.DESCENDING)
                .get()
                .await()
            Result.success(snapshot.toObjects(PlayerOfMonthDto::class.java).map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Svi mjeseci u kojima je igrač osvojio nagradu (za detalje igrača). */
    suspend fun titlesForPlayer(playerId: String): Result<List<PlayerOfMonth>> {
        return try {
            val snapshot = historyCollection().whereEqualTo("playerId", playerId).get().await()
            val titles = snapshot.toObjects(PlayerOfMonthDto::class.java)
                .map { it.toDomain() }
                .sortedByDescending { it.period }
            Result.success(titles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Idempotentno zaključuje PRETHODNI mjesec i sprema pobjednika.
     * Ako zapis za taj mjesec već postoji ili nema glasova - ne radi ništa.
     */
    suspend fun closePreviousMonth(): Result<Unit> {
        val period = VotingPeriod.previous()
        return try {
            val existing = historyCollection().document(period).get().await()
            if (existing.exists()) return Result.success(Unit) // već zaključeno

            val board = leaderboard(period).getOrNull().orEmpty()
            if (board.isEmpty()) return Result.success(Unit) // nije bilo glasova

            val winner = board.first()
            val total = board.sumOf { it.count }
            val photo = playerRepository.getDinamoPlayers().data
                .firstOrNull { it.id == winner.playerId }?.photoUrl

            historyCollection().document(period).set(
                mapOf(
                    "period" to period,
                    "year" to VotingPeriod.year(period).toLong(),
                    "month" to VotingPeriod.month(period).toLong(),
                    "playerId" to winner.playerId,
                    "playerName" to winner.playerName,
                    "playerPhotoUrl" to (photo ?: ""),
                    "votes" to winner.count.toLong(),
                    "totalVotes" to total.toLong(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
