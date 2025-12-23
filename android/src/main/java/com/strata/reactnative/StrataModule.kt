package com.strata.reactnative

import com.facebook.react.bridge.*

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
        }
        promise.resolve(result)
    }
    
    /**
     * Trigger haptic feedback.
     */
    @ReactMethod
    fun triggerHaptics(intensity: String, promise: Promise) {
        // TODO: Implement haptic feedback using Vibrator service
        promise.resolve(null)
    }
    
    private fun getDeviceType(): String {
        val context = reactApplicationContext
        val metrics = context.resources.displayMetrics
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        val diagonalInches = Math.sqrt((widthInches * widthInches + heightInches * heightInches).toDouble())
        
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
