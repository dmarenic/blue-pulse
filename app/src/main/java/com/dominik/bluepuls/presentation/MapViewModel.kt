package com.dominik.bluepuls.presentation

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dominik.bluepuls.core.Constants
import com.dominik.bluepuls.data.LocationRepository
import com.dominik.bluepuls.di.ServiceLocator
import com.dominik.bluepuls.domain.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel za kartu - drži trenutnu lokaciju korisnika i udaljenost do Maksimira.
 */
class MapViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _userLocation = MutableStateFlow<GeoPoint?>(null)
    val userLocation: StateFlow<GeoPoint?> = _userLocation.asStateFlow()

    private val _distanceKm = MutableStateFlow<Float?>(null)
    val distanceKm: StateFlow<Float?> = _distanceKm.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchUserLocation() {
        viewModelScope.launch {
            locationRepository.getCurrentLocation()
                .onSuccess { point ->
                    if (point == null) {
                        // Uspjeh, ali nema fixa (npr. emulator bez postavljene lokacije).
                        _error.value = "Lokacija trenutno nije dostupna. Provjeri je li GPS uključen."
                    } else {
                        _userLocation.value = point
                        _distanceKm.value = distanceToMaksimirKm(point)
                    }
                }
                .onFailure { _error.value = it.localizedMessage ?: "Lokacija nije dostupna." }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun distanceToMaksimirKm(point: GeoPoint): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            point.latitude, point.longitude,
            Constants.MAKSIMIR_LAT, Constants.MAKSIMIR_LNG,
            results
        )
        return results[0] / 1000f
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { MapViewModel(ServiceLocator.locationRepository) }
        }
    }
}
