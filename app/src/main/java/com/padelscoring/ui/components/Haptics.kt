package com.padelscoring.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Tactile Haptic Engine for Padel Up.
 * Following Emil Kowalski craft: immediate, crisp physical feedback for on-court actions.
 */
class PadelHapticFeedback(private val context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Light crisp tick for score increments (+1, +2, undo).
     */
    fun tick() {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(12)
                }
            }
        } catch (_: Throwable) {
            // Graceful fallback for restricted or unsupported vibration
        }
    }

    /**
     * Crisp double click when reaching game point or target match total (e.g. 32 pts).
     */
    fun milestone() {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 20, 40, 20), -1)
                }
            }
        } catch (_: Throwable) {
            // Graceful fallback
        }
    }

    /**
     * Heavy solid thud for match verification or round completion.
     */
    fun heavyConfirm() {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(40)
                }
            }
        } catch (_: Throwable) {
            // Graceful fallback
        }
    }

    /**
     * Warning or dispute pulse.
     */
    fun warning() {
        try {
            if (vibrator?.hasVibrator() == true) {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 35, 60, 35), -1)
            }
        } catch (_: Throwable) {
            // Graceful fallback
        }
    }
}

@Composable
fun rememberPadelHaptics(): PadelHapticFeedback {
    val context = LocalContext.current
    return remember(context) { PadelHapticFeedback(context) }
}
