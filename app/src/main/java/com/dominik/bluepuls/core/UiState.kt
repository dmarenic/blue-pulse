package com.dominik.bluepuls.core

/**
 * Jedinstveni model stanja ekrana kroz cijelu aplikaciju.
 *
 * Svaki ekran koji dohvaća podatke prolazi kroz ova 4 stanja:
 *  - [Loading] dok se podaci učitavaju (prikaži spinner)
 *  - [Success] kad podaci stignu (prikaži sadržaj)
 *  - [Empty]   kad je dohvat uspio ali nema podataka (prikaži poruku)
 *  - [Error]   kad nešto pukne (prikaži poruku + retry)
 *
 * Time je nemoguće da API/Firebase greška sruši aplikaciju - svaka greška
 * postaje samo jedno stanje ekrana.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
