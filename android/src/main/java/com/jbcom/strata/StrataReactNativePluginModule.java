package com.jbcom.strata;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;

public class StrataReactNativePluginModule extends ReactContextBaseJavaModule {
    public static final String NAME = "StrataReactNativePlugin";

    public StrataReactNativePluginModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("platform", "android");
        return constants;
    }

    @ReactMethod
    public void getDeviceInfo(Promise promise) {
        WritableMap map = Arguments.createMap();
        map.putString("platform", "android");
        map.putString("deviceType", isTablet() ? "tablet" : "mobile");
        map.putString("model", Build.MODEL);
        map.putString("brand", Build.BRAND);
        map.putInt("systemVersion", Build.VERSION.SDK_INT);
        promise.resolve(map);
    }

    @ReactMethod
    public void triggerHaptic(String intensity, Promise promise) {
        Vibrator vibrator = (Vibrator) getReactApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            promise.resolve(false);
            return;
        }

        long duration;
        int amplitude;

        switch (intensity) {
            case "light":
                duration = 10;
                amplitude = 50;
                break;
            case "medium":
                duration = 30;
                amplitude = 150;
                break;
            case "heavy":
                duration = 50;
                amplitude = 255;
                break;
            default:
                duration = 20;
                amplitude = 100;
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude));
        } else {
            vibrator.vibrate(duration);
        }
        promise.resolve(true);
    }

    @ReactMethod
    public void setOrientation(String orientation) {
        if (getCurrentActivity() == null) return;

        int orientationConstant = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        if ("portrait".equals(orientation)) {
            orientationConstant = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if ("landscape".equals(orientation)) {
            orientationConstant = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }

        getCurrentActivity().setRequestedOrientation(orientationConstant);
    }

    @ReactMethod
    public void getPerformanceMode(Promise promise) {
        WritableMap map = Arguments.createMap();
        
        // Simple heuristic: low power mode or old device
        boolean isLowPowerMode = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.os.PowerManager powerManager = (android.os.PowerManager) getReactApplicationContext().getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                isLowPowerMode = powerManager.isPowerSaveMode();
            }
        }

        // Check RAM
        android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
        android.app.ActivityManager activityManager = (android.app.ActivityManager) getReactApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            activityManager.getMemoryInfo(mi);
        }
        
        String mode = "high";
        if (isLowPowerMode || mi.totalMem < 2L * 1024 * 1024 * 1024) { // Less than 2GB RAM
            mode = "low";
        } else if (mi.totalMem < 4L * 1024 * 1024 * 1024) { // Less than 4GB RAM
            mode = "medium";
        }

        map.putString("mode", mode);
        map.putBoolean("isLowPowerMode", isLowPowerMode);
        map.putDouble("totalMemory", (double) mi.totalMem);
        promise.resolve(map);
    }

    @ReactMethod
    public void getSafeAreaInsets(Promise promise) {
        WritableMap map = Arguments.createMap();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getCurrentActivity() != null) {
            android.view.Window window = getCurrentActivity().getWindow();
            android.view.View decorView = window.getDecorView();
            android.view.WindowInsets insets = decorView.getRootWindowInsets();
            if (insets != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    android.graphics.Insets i = insets.getInsets(android.view.WindowInsets.Type.systemBars());
                    map.putInt("top", pxToDp(i.top));
                    map.putInt("right", pxToDp(i.right));
                    map.putInt("bottom", pxToDp(i.bottom));
                    map.putInt("left", pxToDp(i.left));
                } else {
                    map.putInt("top", pxToDp(insets.getSystemWindowInsetTop()));
                    map.putInt("right", pxToDp(insets.getSystemWindowInsetRight()));
                    map.putInt("bottom", pxToDp(insets.getSystemWindowInsetBottom()));
                    map.putInt("left", pxToDp(insets.getSystemWindowInsetLeft()));
                }
            } else {
                map.putInt("top", 0);
                map.putInt("right", 0);
                map.putInt("bottom", 0);
                map.putInt("left", 0);
            }
        } else {
            map.putInt("top", 0);
            map.putInt("right", 0);
            map.putInt("bottom", 0);
            map.putInt("left", 0);
        }
        promise.resolve(map);
    }

    private int pxToDp(int px) {
        return (int) (px / getReactApplicationContext().getResources().getDisplayMetrics().density);
    }

    private boolean isTablet() {
        DisplayMetrics metrics = getReactApplicationContext().getResources().getDisplayMetrics();
        float yInches = metrics.heightPixels / metrics.ydpi;
        float xInches = metrics.widthPixels / metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
        return diagonalInches >= 7.0;
    }
}
