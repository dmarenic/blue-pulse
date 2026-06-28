package com.dominik.bluepuls.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.resultDataStore by preferencesDataStore(name = "result_prefs")

/**
 * Pamti ključ zadnje viđenog rezultata (id + rezultat) da bismo o NOVOM
 * rezultatu javili samo jednom.
 */
class ResultPreferences(
    private val context: Context
) {
    private val lastResultKey = stringPreferencesKey("last_result_key")

    val lastResult: Flow<String?> =
        context.resultDataStore.data.map { it[lastResultKey] }

    suspend fun setLastResult(value: String) {
        context.resultDataStore.edit { it[lastResultKey] = value }
    }
}
