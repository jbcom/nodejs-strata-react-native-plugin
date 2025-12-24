import { renderHook } from '@testing-library/react-hooks';
import { useDevice } from '../index';
import { NativeModules, Platform } from 'react-native';

jest.mock('react-native', () => ({
  NativeModules: {
    StrataReactNativePlugin: {
      getDeviceInfo: jest.fn(),
      getSafeAreaInsets: jest.fn(),
      getPerformanceMode: jest.fn(),
    },
  },
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
  it('should return initial device profile', async () => {
    const mockInfo = { deviceType: 'mobile', platform: 'ios' };
    const mockInsets = { top: 47, right: 0, bottom: 34, left: 0 };
    const mockPerf = { mode: 'high' };

    (NativeModules.StrataReactNativePlugin.getDeviceInfo as jest.Mock).mockResolvedValue(mockInfo);
    (NativeModules.StrataReactNativePlugin.getSafeAreaInsets as jest.Mock).mockResolvedValue(mockInsets);
    (NativeModules.StrataReactNativePlugin.getPerformanceMode as jest.Mock).mockResolvedValue(mockPerf);

    const { result, waitForNextUpdate } = renderHook(() => useDevice());

    // Initial state (before useEffect)
    expect(result.current.platform).toBe('ios');
    
    await waitForNextUpdate();

    expect(result.current.safeAreaInsets).toEqual(mockInsets);
    expect(result.current.performanceMode).toBe('high');
  });
});
