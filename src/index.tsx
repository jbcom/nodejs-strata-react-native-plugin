import type React from 'react';
import { useEffect, useState, useCallback, useRef } from 'react';
import { 
  NativeModules, 
  NativeEventEmitter,
  Platform, 
  Dimensions, 
  PixelRatio, 
  View,
  type GestureResponderEvent
} from 'react-native';

const { RNStrata } = NativeModules;

if (!RNStrata && Platform.OS === 'ios') {
  console.warn('RNStrata: Native module not found. Check your native installation.');
}

const eventEmitter = RNStrata ? new NativeEventEmitter(RNStrata) : null;

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
  type?: 'impact' | 'notification' | 'selection';
  intensity?: 'light' | 'medium' | 'heavy' | 'success' | 'warning' | 'error';
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
      if (Platform.OS === 'ios' && RNStrata) {
        try {
          const details = await RNStrata.getDeviceDetails();
          setDeviceProfile({
            ...details,
            performanceMode: 'high', // Default for now as not in RNStrata yet
          });
        } catch (e) {
          console.error('Failed to get native device details', e);
        }
      } else {
        const { width, height } = Dimensions.get('window');
        setDeviceProfile(prev => ({
          ...prev,
          screenWidth: width,
          screenHeight: height,
          orientation: height >= width ? 'portrait' : 'landscape',
        }));
      }
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
    let interval: NodeJS.Timeout;
    
    if (Platform.OS === 'ios' && RNStrata) {
      interval = setInterval(async () => {
        try {
          const snapshot = await RNStrata.getGamepadSnapshot();
          if (snapshot) {
            setInput(prev => ({
              ...snapshot,
              touches: prev.touches, // Preserve touches from provider
            }));
          }
        } catch (_e) {
          // Ignore gamepad errors in poll
        }
      }, 16); // ~60fps
    }

    return () => {
      if (interval) clearInterval(interval);
    };
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

    if (Platform.OS === 'ios' && RNStrata) {
      RNStrata.triggerHaptic(options.type || 'impact', options);
    }
  }, []);

  return { trigger };
}

/**
 * Set screen orientation
 */
export async function setOrientation(_orientation: 'portrait' | 'landscape' | 'default'): Promise<void> {
  // Not implemented in RNStrata yet, but keeping the API
  console.warn('setOrientation is not yet implemented in RNStrata');
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

/**
 * Subscribe to gamepad connection events
 */
export function onGamepadUpdate(callback: (event: { connected: boolean }) => void) {
  if (eventEmitter) {
    return eventEmitter.addListener('onGamepadUpdate', callback);
  }
  return null;
}
