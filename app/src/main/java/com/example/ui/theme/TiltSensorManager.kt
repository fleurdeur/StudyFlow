package com.example.ui.theme

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class TiltVector(
    val tiltX: Float = 0f, // Normalized -1.0 to 1.0
    val tiltY: Float = 0f  // Normalized -1.0 to 1.0
)

val LocalTiltVector = compositionLocalOf { TiltVector() }

@Composable
fun rememberTiltVector(): State<TiltVector> {
    val context = LocalContext.current
    val tiltState = remember { mutableStateOf(TiltVector()) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        var smoothedX = 0f
        var smoothedY = 0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val rawX = event.values[0] / 9.81f
                    val rawY = event.values[1] / 9.81f

                    // Low-pass filter for smooth liquid response
                    smoothedX = smoothedX * 0.85f + rawX * 0.15f
                    smoothedY = smoothedY * 0.85f + rawY * 0.15f

                    tiltState.value = TiltVector(
                        tiltX = (-smoothedX).coerceIn(-1f, 1f),
                        tiltY = (smoothedY).coerceIn(-1f, 1f)
                    )
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    return tiltState
}

// Compute dynamic specular border gradient based on physical phone tilt
fun createDynamicSpecularBorderBrush(
    tilt: TiltVector,
    accentColor: Color,
    isDark: Boolean,
    isEPaper: Boolean = false
): Brush {
    if (isEPaper) {
        return Brush.linearGradient(
            colors = listOf(
                if (isDark) Color(0xFF444444) else Color(0xFF222222),
                if (isDark) Color(0xFF222222) else Color(0xFF666666)
            )
        )
    }

    // Dynamic start and end offsets reacting to accelerometer
    val startX = 0.5f - (tilt.tiltX * 0.5f)
    val startY = 0.2f - (tilt.tiltY * 0.3f)
    val endX = 0.5f + (tilt.tiltX * 0.5f)
    val endY = 0.9f + (tilt.tiltY * 0.3f)

    val highlightColor = if (isDark) {
        accentColor.copy(alpha = 0.45f + (tilt.tiltY * 0.15f).coerceIn(-0.1f, 0.2f))
    } else {
        Color.White.copy(alpha = 0.85f)
    }

    val rimGlow = if (isDark) {
        Color.White.copy(alpha = 0.25f)
    } else {
        accentColor.copy(alpha = 0.30f)
    }

    val shadowRim = if (isDark) Color(0x15FFFFFF) else Color(0x18000000)

    return Brush.linearGradient(
        0.0f to highlightColor,
        0.4f to rimGlow,
        1.0f to shadowRim,
        start = Offset(startX * 800f, startY * 800f),
        end = Offset(endX * 800f, endY * 800f)
    )
}
