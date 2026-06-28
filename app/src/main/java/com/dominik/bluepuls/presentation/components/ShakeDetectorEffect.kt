package com.dominik.bluepuls.presentation.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dominik.bluepuls.core.ShakeDetector

/**
 * Composable koji registrira shake detektor vezan uz lifecycle ekrana.
 *
 * Sluša SAMO dok je ekran u prvom planu (ON_RESUME -> ON_PAUSE) i uredno se
 * odjavljuje u onDispose -> nema memory leaka ni trošenja baterije u pozadini.
 */
@Composable
fun ShakeDetectorEffect(onShake: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // rememberUpdatedState -> uvijek se poziva najnovija onShake lambda bez ponovne registracije.
    val currentOnShake by rememberUpdatedState(onShake)

    DisposableEffect(lifecycleOwner) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val detector = ShakeDetector { currentOnShake() }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> accelerometer?.let {
                    sensorManager.registerListener(detector, it, SensorManager.SENSOR_DELAY_NORMAL)
                }
                Lifecycle.Event.ON_PAUSE -> sensorManager?.unregisterListener(detector)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            sensorManager?.unregisterListener(detector)
        }
    }
}
