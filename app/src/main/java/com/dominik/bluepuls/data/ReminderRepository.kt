package com.dominik.bluepuls.data

import android.content.Context
import com.dominik.bluepuls.R
import com.dominik.bluepuls.core.MatchDateTime
import com.dominik.bluepuls.domain.Match
import com.dominik.bluepuls.notifications.MatchReminderScheduler
import kotlinx.coroutines.flow.Flow

/**
 * Repozitorij za podsjetnike: spaja WorkManager zakazivanje (stvarno vrijeme)
 * s lokalnim stanjem (DataStore) koje pamti za koje su utakmice postavljeni.
 */
class ReminderRepository(
    private val context: Context,
    private val reminderPreferences: ReminderPreferences
) {
    val remindedMatchIds: Flow<Set<String>> = reminderPreferences.remindedMatchIds

    /** Postavlja podsjetnik za utakmicu. Vraća false ako je već odigrana. */
    suspend fun setReminder(match: Match): Boolean {
        val kickoff = match.kickoffMillis() ?: return false
        val title = context.getString(R.string.notif_title)
        val message = context.getString(
            R.string.notif_message_match,
            match.homeTeam.name,
            match.awayTeam.name,
            MatchDateTime.formatted(match.date, match.time)
        )
        val notificationId = reminderPreferences.notificationIdFor(match.id)
        val scheduled = MatchReminderScheduler.scheduleForMatch(
            context, match.id, notificationId, title, message, kickoff
        )
        if (scheduled) reminderPreferences.add(match.id)
        return scheduled
    }

    suspend fun cancelReminder(matchId: String) {
        MatchReminderScheduler.cancel(context, matchId)
        reminderPreferences.remove(matchId)
    }
}
