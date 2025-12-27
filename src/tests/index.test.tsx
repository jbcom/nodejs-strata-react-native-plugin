import { renderHook } from '@testing-library/react-hooks/native';
import { useDevice } from '../index';
import { NativeModules } from 'react-native';

jest.mock('react-native', () => ({
  NativeModules: {
    RNStrata: {
      getDeviceDetails: jest.fn(),
      getGamepadSnapshot: jest.fn(),
      triggerHaptic: jest.fn(),
    },
  },
  NativeEventEmitter: jest.fn(() => ({
    addListener: jest.fn(),
    removeListeners: jest.fn(),
  })),
  Platform: {
    OS: 'ios',
    select: jest.fn(obj => obj.ios),
  },
  Dimensions: {
    get: jest.fn(() => ({ width: 390, height: 844 })),
    addEventListener: jest.fn(() => ({ remove: jest.fn() })),
  },
  PixelRatio: {
    get: jest.fn(() => 3),
  },
}));

describe('useDevice', () => {
  it('should return initial device profile', () => {
    const mockDetails = { 
      deviceType: 'mobile', 
      platform: 'ios',
      inputMode: 'touch',
      orientation: 'portrait',
      hasTouch: true,
      hasGamepad: false,
      screenWidth: 390,
      screenHeight: 844,
      pixelRatio: 3,
      safeAreaInsets: { top: 47, right: 0, bottom: 34, left: 0 }
    };

    (NativeModules.RNStrata.getDeviceDetails as jest.Mock).mockResolvedValue(mockDetails);

    const { result } = renderHook(() => useDevice());

    // Initial state
    expect(result.current.platform).toBe('ios');
    expect(result.current.hasTouch).toBe(true);
  });
});
