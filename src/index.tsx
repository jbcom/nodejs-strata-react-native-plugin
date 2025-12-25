import React, { useEffect, useState, useCallback, useRef } from 'react';
import { 
  NativeModules, 
  Platform, 
  Dimensions, 
  PixelRatio, 
  View,
  type GestureResponderEvent
} from 'react-native';

const { StrataReactNativePlugin } = NativeModules;

if (!StrataReactNativePlugin && Platform.OS !== 'web') {
  console.warn('StrataReactNativePlugin: Native module not found. Check your native installation.');
}

export interface DeviceProfile {
  deviceType: 'mobile' | 'tablet' | 'foldable' | 'desktop';
  platform: 'ios' | 'android' | 'web';
  inputMode: 'touch' | 'keyboard' | 'gamepad' | 'hybrid';
  orientation: 'portrait' | 'landscape';
  hasTouch: boolean;
  hasGamepad: boolean;
  screenWidth: number;
  screenHeight: number;
  pixelRatio: number;
  safeAreaInsets: {
    top: number;
    right: number;
    bottom: number;
    left: number;
  };
  performanceMode: 'low' | 'medium' | 'high';
}

export interface TouchState {
  identifier: string;
  pageX: number;
  pageY: number;
  locationX: number;
  locationY: number;
  timestamp: number;
}

export interface InputSnapshot {
  timestamp: number;
  leftStick: { x: number; y: number };
  rightStick: { x: number; y: number };
  buttons: Record<string, boolean>;
  triggers: { left: number; right: number };
  touches: TouchState[];
}

export interface HapticsOptions {
  intensity?: 'light' | 'medium' | 'heavy';
  customIntensity?: number;
  duration?: number;
  pattern?: number[];
}

/**
 * Hook to get and track device information
 */
export function useDevice(): DeviceProfile {
  const [deviceProfile, setDeviceProfile] = useState<DeviceProfile>(() => {
    const { width, height } = Dimensions.get('window');
    return {
      deviceType: 'mobile',
      platform: Platform.OS as 'ios' | 'android' | 'web',
      inputMode: 'touch',
      orientation: height >= width ? 'portrait' : 'landscape',
      hasTouch: true,
      hasGamepad: false,
      screenWidth: width,
      screenHeight: height,
      pixelRatio: PixelRatio.get(),
      safeAreaInsets: { top: 0, right: 0, bottom: 0, left: 0 },
      performanceMode: 'high',
    };
  });

  useEffect(() => {
    const updateDeviceInfo = async () => {
      const { width, height } = Dimensions.get('window');
      
      let nativeInfo: any = { deviceType: 'mobile', platform: Platform.OS };
      let safeArea = { top: 0, right: 0, bottom: 0, left: 0 };
      let performance = { mode: 'high' };

      if (StrataReactNativePlugin) {
        try {
          // Use newer combined API if available
          const profile = await (StrataReactNativePlugin.getDeviceProfile ? 
            StrataReactNativePlugin.getDeviceProfile() : 
            StrataReactNativePlugin.getDeviceInfo());
          
          nativeInfo = profile;
          safeArea = profile.safeAreaInsets || { top: 0, right: 0, bottom: 0, left: 0 };
          performance = { mode: profile.performanceMode || profile.mode || 'high' };
        } catch (e) {
          try {
            const [info, insets, perf] = await Promise.all([
              StrataReactNativePlugin.getDeviceInfo(),
              StrataReactNativePlugin.getSafeAreaInsets(),
              StrataReactNativePlugin.getPerformanceMode()
            ]);
            nativeInfo = info;
            safeArea = insets;
            performance = perf;
          } catch (e2) {
            console.error('Failed to get native device info', e2);
          }
        }
      }

      setDeviceProfile(prev => ({
        ...prev,
        ...nativeInfo,
        deviceType: (nativeInfo.deviceType as any) || 'mobile',
        orientation: height >= width ? 'portrait' : 'landscape',
        screenWidth: width,
        screenHeight: height,
        safeAreaInsets: safeArea || { top: 0, right: 0, bottom: 0, left: 0 },
        performanceMode: (performance.mode as any) || 'high',
      }));
    };

    updateDeviceInfo();

    const subscription = Dimensions.addEventListener('change', updateDeviceInfo);
    return () => {
      if (subscription?.remove) {
        subscription.remove();
      }
    };
  }, []);

  return deviceProfile;
}

/**
 * Hook to handle input state
 */
export function useInput(): InputSnapshot {
  const [input, setInput] = useState<InputSnapshot>({
    timestamp: Date.now(),
    leftStick: { x: 0, y: 0 },
    rightStick: { x: 0, y: 0 },
    buttons: {},
    triggers: { left: 0, right: 0 },
    touches: [],
  });

  useEffect(() => {
    if (!StrataReactNativePlugin || Platform.OS === 'web') return;

    const interval = setInterval(async () => {
      try {
        const snapshot = await StrataReactNativePlugin.getInputSnapshot();
        if (snapshot) {
          setInput(prev => ({
            ...prev,
            ...snapshot,
            touches: prev.touches // Keep JS-side touches
          }));
        }
      } catch (e) {
        // Silently fail polling
      }
    }, 16); // ~60fps poll for native input

    return () => clearInterval(interval);
  }, []);

  return input;
}

/**
 * A component that captures touch gestures and provides them to the game
 */
export const StrataInputProvider: React.FC<{
  children: React.ReactNode;
  onInput?: (snapshot: InputSnapshot) => void;
}> = ({ children, onInput }) => {
  const touches = useRef<Map<string, TouchState>>(new Map());

  const updateTouches = (event: GestureResponderEvent) => {
    const changedTouches = event.nativeEvent.changedTouches;
    const timestamp = event.nativeEvent.timestamp;

    changedTouches.forEach(touch => {
      touches.current.set(touch.identifier, {
        identifier: touch.identifier,
        pageX: touch.pageX,
        pageY: touch.pageY,
        locationX: touch.locationX,
        locationY: touch.locationY,
        timestamp,
      });
    });

    const snapshot: InputSnapshot = {
      timestamp,
      leftStick: { x: 0, y: 0 },
      rightStick: { x: 0, y: 0 },
      buttons: {},
      triggers: { left: 0, right: 0 },
      touches: Array.from(touches.current.values()),
    };

    onInput?.(snapshot);
  };

  const removeTouches = (event: GestureResponderEvent) => {
    const changedTouches = event.nativeEvent.changedTouches;
    changedTouches.forEach(touch => {
      touches.current.delete(touch.identifier);
    });

    const snapshot: InputSnapshot = {
      timestamp: event.nativeEvent.timestamp,
      leftStick: { x: 0, y: 0 },
      rightStick: { x: 0, y: 0 },
      buttons: {},
      triggers: { left: 0, right: 0 },
      touches: Array.from(touches.current.values()),
    };

    onInput?.(snapshot);
  };

  return (
    <View 
      style={{ flex: 1 }}
      onStartShouldSetResponder={() => true}
      onMoveShouldSetResponder={() => true}
      onResponderGrant={updateTouches}
      onResponderMove={updateTouches}
      onResponderRelease={removeTouches}
      onResponderTerminate={removeTouches}
    >
      {children}
    </View>
  );
};

/**
 * Hook for haptic feedback
 */
export function useHaptics(): { trigger: (options: HapticsOptions) => Promise<void> } {
  const trigger = useCallback(async (options: HapticsOptions) => {
    if (Platform.OS === 'web') {
      if ('vibrate' in navigator) {
        navigator.vibrate(options.duration || 50);
      }
      return;
    }

    if (StrataReactNativePlugin) {
      if (StrataReactNativePlugin.triggerHaptics) {
        await StrataReactNativePlugin.triggerHaptics(options);
      } else {
        await StrataReactNativePlugin.triggerHaptic(options.intensity || 'medium');
      }
    }
  }, []);

  return { trigger };
}

/**
 * Set screen orientation
 */
export async function setOrientation(orientation: 'portrait' | 'landscape' | 'default'): Promise<void> {
  if (StrataReactNativePlugin) {
    await StrataReactNativePlugin.setOrientation(orientation);
  }
}

/**
 * Hook for control hints based on device and input
 */
export function useControlHints(): { movement: string; action: string; camera: string } {
  const { inputMode, hasGamepad } = useDevice();
  
  if (hasGamepad || inputMode === 'gamepad') {
    return {
      movement: 'Left Stick',
      action: 'Button A / X',
      camera: 'Right Stick'
    };
  }
  
  if (inputMode === 'touch') {
    return {
      movement: 'Virtual Joystick',
      action: 'Tap Screen',
      camera: 'Drag'
    };
  }
  
  return {
    movement: 'WASD / Left Stick',
    action: 'Space / Button A',
    camera: 'Mouse / Right Stick'
  };
}
