package com.dominik.bluepuls.data

import android.annotation.SuppressLint
import android.content.Context
import com.dominik.bluepuls.domain.GeoPoint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

/**
 * Repozitorij za GPS lokaciju (FusedLocationProvider).
 * Pozivatelj je dužan osigurati da je dozvola odobrena; ako nije,
 * SecurityException se hvata i vraća kao Result.failure (bez crasha).
 */
class LocationRepository(
    context: Context
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<GeoPoint?> {
        return try {
            val cancellationToken = CancellationTokenSource().token
            val location = fusedClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationToken)
                .await()
            Result.success(location?.let { GeoPoint(it.latitude, it.longitude) })
        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
