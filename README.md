# @jbcom/strata-react-native-plugin

React Native plugin for [Strata 3D](https://github.com/jbcom/nodejs-strata) - cross-platform input, device detection, and haptics for mobile games.

## Features

- **Device Detection**: identify device type (mobile, tablet), platform (iOS, Android, Web), and performance capabilities.
- **Input Handling**: Unified touch input handling with `StrataInputProvider`.
- **Haptic Feedback**: Cross-platform vibration with intensity control (iOS Taptic Engine, Android Vibrator).
- **Safe Area Insets**: Native safe area detection for notches and system UI.
- **Orientation**: Get and set screen orientation (portrait, landscape).
- **Performance Mode**: Detect low power mode and hardware performance levels.

## Installation

```bash
npm install @jbcom/strata-react-native-plugin
```

### Native Setup

#### iOS
```bash
cd ios && pod install
```

#### Android
Automatically linked.

## API Reference

### Hooks

#### `useDevice()`
Returns the current `DeviceProfile`.

```typescript
const { platform, deviceType, orientation, safeAreaInsets, performanceMode } = useDevice();
```

#### `useInput()`
Returns the current `InputSnapshot` (requires `StrataInputProvider`).

```typescript
const input = useInput();
```

#### `useHaptics()`
Returns a trigger function for haptic feedback.

```typescript
const { trigger } = useHaptics();
await trigger({ intensity: 'light' | 'medium' | 'heavy' });
```

#### `useControlHints()`
Returns localized control hints based on current input mode.

### Components

#### `StrataInputProvider`
Wraps your application to capture and process input events.

```tsx
<StrataInputProvider onInput={(snapshot) => handleInput(snapshot)}>
  <GameView />
</StrataInputProvider>
```

### Utilities

#### `setOrientation(orientation)`
Programs the device orientation.

```typescript
import { setOrientation } from '@jbcom/strata-react-native-plugin';
await setOrientation('landscape');
```

## Documentation

Full documentation is available in the [docs](./docs) directory.

## Related

- [@jbcom/strata](https://github.com/jbcom/nodejs-strata) - Main library
- [@jbcom/strata-capacitor-plugin](https://github.com/jbcom/nodejs-strata-capacitor-plugin) - Capacitor version
- [@jbcom/strata-examples](https://github.com/jbcom/nodejs-strata-examples) - Example applications

## License

MIT
\n\n<!-- CI trigger -->
