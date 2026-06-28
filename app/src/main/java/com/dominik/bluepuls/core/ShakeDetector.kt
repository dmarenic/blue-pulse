package com.dominik.bluepuls.core

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Detektira protresanje uređaja preko akcelerometra.
 *
 * gForce = ukupna akceleracija / gravitacija Zemlje. Kad prijeđe prag
 * [Constants.SHAKE_THRESHOLD_GRAVITY], okida [onShake] (uz debounce od 1s
 * da jedno protresanje ne okine više puta).
 */
class ShakeDetector(
    private val onShake: () -> Unit
) : SensorEventListener {

    private var lastShakeTimestamp = 0L

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH

        if (gForce > Constants.SHAKE_THRESHOLD_GRAVITY) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp > DEBOUNCE_MS) {
                lastShakeTimestamp = now
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* nije potrebno */ }

    private companion object {
        const val DEBOUNCE_MS = 1000L
    }
}
