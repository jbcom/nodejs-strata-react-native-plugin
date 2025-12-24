package com.jbcom.strata

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.InputDevice
import android.view.WindowManager
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class StrataModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), LifecycleEventListener {

    private var isListening = false
    private val gamepadState = Arguments.createMap()
    private val buttons = Arguments.createMap()
    private val leftStick = Arguments.createMap()
    private val rightStick = Arguments.createMap()
    private val triggers = Arguments.createMap()

    init {
        reactContext.addLifecycleEventListener(this)
        resetGamepadState()
    }

    private fun resetGamepadState() {
        leftStick.putDouble("x", 0.0)
        leftStick.putDouble("y", 0.0)
        rightStick.putDouble("x", 0.0)
        rightStick.putDouble("y", 0.0)
        triggers.putDouble("left", 0.0)
        triggers.putDouble("right", 0.0)
        
        // Initialize buttons (common ones)
        val buttonList = listOf("a", "b", "x", "y", "l1", "r1", "l2", "r2", "start", "select", "dpadUp", "dpadDown", "dpadLeft", "dpadRight")
        for (button in buttonList) {
            buttons.putBoolean(button, false)
        }
    }

    override fun getName(): String {
        return "Strata"
    }

    override fun onHostResume() {
        startListening()
    }

    override fun onHostPause() {
        stopListening()
    }

    override fun onHostDestroy() {
        stopListening()
    }

    private fun startListening() {
        if (isListening) return
        // In a real implementation, we would hook into the Activity's events
        // or attach listeners to the root view.
        // For now, we'll mark it as listening.
        isListening = true
    }

    private fun stopListening() {
        isListening = false
    }

    @ReactMethod
    fun getDeviceProfile(promise: Promise) {
        try {
            val context = reactApplicationContext
            val metrics = context.resources.displayMetrics
            val configuration = context.resources.configuration

            val profile = Arguments.createMap()
            
            // Device Type
            val deviceType = when {
                isFoldable() -> "foldable"
                (configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE -> "tablet"
                else -> "mobile"
            }
            profile.putString("deviceType", deviceType)
            profile.putString("platform", "android")
            
            // Input Mode (Default to touch, check for gamepads later)
            profile.putString("inputMode", "touch")
            
            // Orientation
            val orientation = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) "landscape" else "portrait"
            profile.putString("orientation", orientation)
            
            profile.putBoolean("hasTouch", context.packageManager.hasSystemFeature("android.hardware.touchscreen"))
            profile.putBoolean("hasGamepad", hasGamepadConnected())
            
            profile.putDouble("screenWidth", metrics.widthPixels.toDouble() / metrics.density)
            profile.putDouble("screenHeight", metrics.heightPixels.toDouble() / metrics.density)
            profile.putDouble("pixelRatio", metrics.density.toDouble())

            // Safe Area Insets (Initial values, might need more complex logic for dynamic updates)
            profile.putMap("safeAreaInsets", getSafeAreaInsetsMap())

            promise.resolve(profile)
        } catch (e: Exception) {
            promise.reject("ERR_DEVICE_PROFILE", e.message)
        }
    }

    @ReactMethod
    fun triggerHaptics(options: ReadableMap, promise: Promise) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = reactApplicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            reactApplicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (!vibrator.hasVibrator()) {
            promise.resolve(false)
            return
        }

        val duration = if (options.hasKey("duration")) options.getInt("duration").toLong() else 50L
        val intensity = if (options.hasKey("customIntensity")) (options.getDouble("customIntensity") * 255).toInt() else 128

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, intensity.coerceIn(1, 255)))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
        promise.resolve(true)
    }

    @ReactMethod
    fun getInputSnapshot(promise: Promise) {
        val snapshot = Arguments.createMap()
        snapshot.putDouble("timestamp", System.currentTimeMillis().toDouble())
        
        val snapshotButtons = Arguments.createMap()
        val buttonsIter = buttons.keySetIterator()
        while (buttonsIter.hasNextKey()) {
            val key = buttonsIter.nextKey()
            snapshotButtons.putBoolean(key, buttons.getBoolean(key))
        }
        snapshot.putMap("buttons", snapshotButtons)

        val snapshotLeftStick = Arguments.createMap()
        snapshotLeftStick.putDouble("x", leftStick.getDouble("x"))
        snapshotLeftStick.putDouble("y", leftStick.getDouble("y"))
        snapshot.putMap("leftStick", snapshotLeftStick)

        val snapshotRightStick = Arguments.createMap()
        snapshotRightStick.putDouble("x", rightStick.getDouble("x"))
        snapshotRightStick.putDouble("y", rightStick.getDouble("y"))
        snapshot.putMap("rightStick", snapshotRightStick)

        val snapshotTriggers = Arguments.createMap()
        snapshotTriggers.putDouble("left", triggers.getDouble("left"))
        snapshotTriggers.putDouble("right", triggers.getDouble("right"))
        snapshot.putMap("triggers", snapshotTriggers)
        
        val gamepads = Arguments.createArray()
        val deviceIds = InputDevice.getDeviceIds()
        for (deviceId in deviceIds) {
            val device = InputDevice.getDevice(deviceId)
            if (device != null && (device.sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD ||
                device.sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK)) {
                gamepads.pushInt(deviceId)
            }
        }
        
        snapshot.putArray("connectedGamepads", gamepads)
        promise.resolve(snapshot)
    }

    // Methods to be called from MainActivity to update state
    fun handleKeyEvent(event: KeyEvent) {
        val isDown = event.action == KeyEvent.ACTION_DOWN
        val button = when (event.keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> "a"
            KeyEvent.KEYCODE_BUTTON_B -> "b"
            KeyEvent.KEYCODE_BUTTON_X -> "x"
            KeyEvent.KEYCODE_BUTTON_Y -> "y"
            KeyEvent.KEYCODE_BUTTON_L1 -> "l1"
            KeyEvent.KEYCODE_BUTTON_R1 -> "r1"
            KeyEvent.KEYCODE_BUTTON_L2 -> "l2"
            KeyEvent.KEYCODE_BUTTON_R2 -> "r2"
            KeyEvent.KEYCODE_BUTTON_START -> "start"
            KeyEvent.KEYCODE_BUTTON_SELECT -> "select"
            KeyEvent.KEYCODE_DPAD_UP -> "dpadUp"
            KeyEvent.KEYCODE_DPAD_DOWN -> "dpadDown"
            KeyEvent.KEYCODE_DPAD_LEFT -> "dpadLeft"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "dpadRight"
            else -> null
        }
        button?.let { buttons.putBoolean(it, isDown) }
    }

    fun handleMotionEvent(event: MotionEvent) {
        if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK &&
            event.action == MotionEvent.ACTION_MOVE) {
            
            leftStick.putDouble("x", event.getAxisValue(MotionEvent.AXIS_X).toDouble())
            leftStick.putDouble("y", event.getAxisValue(MotionEvent.AXIS_Y).toDouble())
            
            rightStick.putDouble("x", event.getAxisValue(MotionEvent.AXIS_Z).toDouble())
            rightStick.putDouble("y", event.getAxisValue(MotionEvent.AXIS_RZ).toDouble())
            
            triggers.putDouble("left", event.getAxisValue(MotionEvent.AXIS_BRAKE).toDouble())
            triggers.putDouble("right", event.getAxisValue(MotionEvent.AXIS_GAS).toDouble())
        }
    }

    private fun isFoldable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Check for foldable features
            return reactApplicationContext.packageManager.hasSystemFeature("android.hardware.sensor.hinge_angle")
        }
        return false
    }

    private fun hasGamepadConnected(): Boolean {
        val deviceIds = InputDevice.getDeviceIds()
        for (deviceId in deviceIds) {
            val device = InputDevice.getDevice(deviceId)
            if (device != null) {
                val sources = device.sources
                if (sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD ||
                    sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) {
                    return true
                }
            }
        }
        return false
    }

    private fun getSafeAreaInsetsMap(): ReadableMap {
        val insets = Arguments.createMap()
        val density = reactApplicationContext.resources.displayMetrics.density
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val windowManager = reactApplicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowInsets = windowManager.defaultDisplay.cutout?.let { cutout ->
                // Basic implementation for display cutouts
                insets.putDouble("top", cutout.safeInsetTop.toDouble() / density)
                insets.putDouble("right", cutout.safeInsetRight.toDouble() / density)
                insets.putDouble("bottom", cutout.safeInsetBottom.toDouble() / density)
                insets.putDouble("left", cutout.safeInsetLeft.toDouble() / density)
                return@let true
            } ?: false
            
            if (!windowInsets) {
                // Fallback to status bar height for top inset
                val resourceId = reactApplicationContext.resources.getIdentifier("status_bar_height", "dimen", "android")
                val statusBarHeight = if (resourceId > 0) {
                    reactApplicationContext.resources.getDimensionPixelSize(resourceId).toDouble() / density
                } else {
                    0.0
                }
                insets.putDouble("top", statusBarHeight)
                insets.putDouble("right", 0.0)
                insets.putDouble("bottom", 0.0)
                insets.putDouble("left", 0.0)
            }
        } else {
            insets.putDouble("top", 0.0)
            insets.putDouble("right", 0.0)
            insets.putDouble("bottom", 0.0)
            insets.putDouble("left", 0.0)
        }
        
        return insets
    }
}
