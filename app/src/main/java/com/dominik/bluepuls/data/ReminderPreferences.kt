package com.dominik.bluepuls.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Zasebna DataStore instanca za podsjetnike.
private val Context.reminderDataStore by preferencesDataStore(name = "reminder_prefs")

/**
 * Lokalno pamti za koje utakmice je korisnik postavio podsjetnik (skup ID-eva)
 * te stabilan, jedinstven notification ID po utakmici.
 * Omogućuje UI-u da prikaže stanje (postavljen / nije) i da ga može otkazati.
 */
class ReminderPreferences(
    private val context: Context
) {
    private val remindedKey = stringSetPreferencesKey("reminded_match_ids")
    private val nextNotificationIdKey = intPreferencesKey("next_notification_id")
    private fun notificationIdKey(matchId: String) = intPreferencesKey("notification_id_$matchId")

    val remindedMatchIds: Flow<Set<String>> =
        context.reminderDataStore.data.map { prefs -> prefs[remindedKey] ?: emptySet() }

    suspend fun add(matchId: String) {
        context.reminderDataStore.edit { prefs ->
            prefs[remindedKey] = (prefs[remindedKey] ?: emptySet()) + matchId
        }
    }

    suspend fun remove(matchId: String) {
        context.reminderDataStore.edit { prefs ->
            prefs[remindedKey] = (prefs[remindedKey] ?: emptySet()) - matchId
        }
    }

    /**
     * Vraća stabilan, jedinstven notification ID za [matchId]. Prvi put dodjeljuje
     * sljedeći slobodni broj iz monotonog brojača i trajno ga zapamti; svaki idući
     * poziv za istu utakmicu vraća isti ID. Time nema kolizija (za razliku od
     * hashCode-a) ni prepisivanja notifikacija različitih utakmica.
     */
    suspend fun notificationIdFor(matchId: String): Int {
        val key = notificationIdKey(matchId)
        var assigned = NOTIFICATION_ID_BASE
        context.reminderDataStore.edit { prefs ->
            val existing = prefs[key]
            if (existing != null) {
                assigned = existing
            } else {
                val next = prefs[nextNotificationIdKey] ?: NOTIFICATION_ID_BASE
                assigned = next
                prefs[key] = next
                prefs[nextNotificationIdKey] = next + 1
            }
        }
        return assigned
    }

    private companion object {
        // Iznad ID-a notifikacije rezultata (2002) da se kanali ne preklapaju.
        const val NOTIFICATION_ID_BASE = 3000
    }
}
