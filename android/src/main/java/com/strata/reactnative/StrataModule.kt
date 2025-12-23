package com.strata.reactnative

import com.facebook.react.bridge.*
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Strata React Native module for Android.
 * 
 * Provides device detection, input handling, and haptic feedback.
 */
class StrataModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    
    override fun getName(): String = "StrataModule"
    
    /**
     * Get device profile information.
     */
    @ReactMethod
    fun getDeviceProfile(promise: Promise) {
        val result = Arguments.createMap().apply {
            putString("platform", "android")
            putString("deviceType", getDeviceType())
            putBoolean("hasTouch", true)
            putBoolean("hasGamepad", false) // TODO: detect gamepad
            putDouble("screenWidth", getScreenWidth())
            putDouble("screenHeight", getScreenHeight())
            putDouble("pixelRatio", getPixelRatio())
            putMap("safeAreaInsets", getSafeAreaInsets())
        }
        promise.resolve(result)
    }
    
    /**
     * Trigger haptic feedback.
     */
    @ReactMethod
    fun triggerHaptics(intensity: String, promise: Promise) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = reactApplicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                reactApplicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            if (!vibrator.hasVibrator()) {
                promise.resolve(null)
                return
            }
            
            val effect = when (intensity) {
                "light" -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                "heavy" -> VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                else -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE) // medium
            }
            
            vibrator.vibrate(effect)
    @ReactMethod
    fun triggerHaptics(intensity: String, promise: Promise) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = reactApplicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                reactApplicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            if (!vibrator.hasVibrator()) {
                promise.resolve(null)
                return
            }
            
            val effect = when (intensity) {
                "light" -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                "heavy" -> VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                else -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE) // medium
            }
            
            vibrator.vibrate(effect)
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("HAPTICS_ERROR", "Failed to trigger haptics: ${e.message}")
        }
    }
    
    private fun getDeviceType(): String {
        val context = reactApplicationContext
        val metrics = context.resources.displayMetrics
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        val diagonalInches = Math.hypot(widthInches.toDouble(), heightInches.toDouble())
        
        return when {
            diagonalInches < 7 -> "mobile"
            diagonalInches < 10 -> "tablet"
            else -> "tablet"
        }
    }
    
    private fun getScreenWidth(): Double {
        return reactApplicationContext.resources.displayMetrics.widthPixels.toDouble()
    }
    
    private fun getScreenHeight(): Double {
        return reactApplicationContext.resources.displayMetrics.heightPixels.toDouble()
    }
    
    private fun getPixelRatio(): Double {
        return reactApplicationContext.resources.displayMetrics.density.toDouble()
    }
}
