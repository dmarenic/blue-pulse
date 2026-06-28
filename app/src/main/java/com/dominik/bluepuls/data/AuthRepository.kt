package com.dominik.bluepuls.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Repozitorij za Firebase Authentication.
 * FirebaseAuth se injektira (DI) radi testabilnosti.
 */
class AuthRepository(
    private val auth: FirebaseAuth
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Prijava nije uspjela."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Registracija nije uspjela."))
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            // Firebase Auth već jamči jedinstvenost e-maila - bez dodatnog Firestore pretraživanja.
            Result.failure(IllegalStateException("Korisnik s ovom e-mail adresom već postoji."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
