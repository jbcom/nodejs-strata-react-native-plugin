import { renderHook } from '@testing-library/react-hooks';
import { useDevice } from '../index';
import { NativeModules } from 'react-native';

jest.mock('react-native', () => ({
  NativeModules: {
    StrataModule: {
      getDeviceProfile: jest.fn(),
    },
    StrataReactNativePlugin: {
      getDeviceInfo: jest.fn(),
      getSafeAreaInsets: jest.fn(),
      getPerformanceMode: jest.fn(),
    }
  },
  Platform: {
    OS: 'ios',
    select: jest.fn((obj: Record<string, unknown>) => obj.ios),
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
    const mockProfile = { 
      deviceType: 'mobile', 
      platform: 'ios',
      safeAreaInsets: { top: 47, right: 0, bottom: 34, left: 0 },
      performanceMode: 'high'
    };

    (NativeModules.StrataModule.getDeviceProfile as jest.Mock).mockResolvedValue(mockProfile);

    const { result, waitForNextUpdate } = renderHook(() => useDevice());

    // Initial state (before useEffect)
    expect(result.current.platform).toBe('ios');
    
    await waitForNextUpdate();

    expect(result.current.safeAreaInsets).toEqual(mockProfile.safeAreaInsets);
    expect(result.current.performanceMode).toBe('high');
  });
});
