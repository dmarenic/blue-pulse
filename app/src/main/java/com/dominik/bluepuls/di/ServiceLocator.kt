package com.dominik.bluepuls.di

import android.content.Context
import androidx.room.Room
import com.dominik.bluepuls.data.AuthRepository
import com.dominik.bluepuls.data.ClubRepository
import com.dominik.bluepuls.data.ImageUploader
import com.dominik.bluepuls.data.LocationRepository
import com.dominik.bluepuls.data.MatchRepository
import com.dominik.bluepuls.data.PhotoRepository
import com.dominik.bluepuls.data.PlayerRepository
import com.dominik.bluepuls.data.ProfileRepository
import com.dominik.bluepuls.data.ReminderPreferences
import com.dominik.bluepuls.data.ReminderRepository
import com.dominik.bluepuls.data.ResultPreferences
import com.dominik.bluepuls.data.RetrofitInstance
import com.dominik.bluepuls.data.StandingsRepository
import com.dominik.bluepuls.data.VoteRepository
import com.dominik.bluepuls.data.local.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Jednostavan, ručni Dependency Injection kontejner.
 * Sve zavisnosti se stvaraju ovdje i injektiraju u ViewModele preko Factory-ja.
 * Inicijalizira se jednom u [com.dominik.bluepuls.BluePulseApp.onCreate].
 */
object ServiceLocator {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val applicationContext: Context get() = appContext

    // --- Firebase singletoni ---
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Upload slika preko imgbb-a (zamjena Firebase Storagea, radi na Spark planu).
    private val imageUploader: ImageUploader by lazy {
        ImageUploader(appContext, RetrofitInstance.imageUploadApi)
    }

    // --- Lokalna pohrana ---
    private val reminderPreferences: ReminderPreferences by lazy { ReminderPreferences(appContext) }
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(appContext, AppDatabase::class.java, "bluepulse.db")
            // Promjena sheme (dodан klub) -> presloži cache; briše zastarjele zapise.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    // --- Repozitoriji ---
    val authRepository: AuthRepository by lazy { AuthRepository(firebaseAuth) }
    val matchRepository: MatchRepository by lazy { MatchRepository(RetrofitInstance.api, database.matchDao()) }
    val playerRepository: PlayerRepository by lazy { PlayerRepository(RetrofitInstance.api, database.playerDao()) }
    val clubRepository: ClubRepository by lazy { ClubRepository(RetrofitInstance.api) }
    val standingsRepository: StandingsRepository by lazy { StandingsRepository(RetrofitInstance.api) }
    val profileRepository: ProfileRepository by lazy { ProfileRepository(firestore, imageUploader) }
    val voteRepository: VoteRepository by lazy { VoteRepository(firestore, firebaseAuth, playerRepository) }
    val reminderRepository: ReminderRepository by lazy { ReminderRepository(appContext, reminderPreferences) }
    val resultPreferences: ResultPreferences by lazy { ResultPreferences(appContext) }
    val photoRepository: PhotoRepository by lazy { PhotoRepository(firestore, imageUploader) }
    val locationRepository: LocationRepository by lazy { LocationRepository(appContext) }
}
