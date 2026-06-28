package com.dominik.bluepuls

import android.app.Application
import com.dominik.bluepuls.di.ServiceLocator
import com.dominik.bluepuls.notifications.MatchResultScheduler

/**
 * Glavna Application klasa.
 * Inicijalizira ručni DI kontejner (ServiceLocator) jednom pri pokretanju app-a
 * i zakazuje periodičnu provjeru rezultata utakmica.
 * Registrirana je u AndroidManifest.xml preko android:name.
 */
class BluePulseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        MatchResultScheduler.schedule(this)
    }
}