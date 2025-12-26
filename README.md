# @strata/react-native-plugin

[![npm version](https://img.shields.io/npm/v/@strata/react-native-plugin.svg)](https://www.npmjs.com/package/@strata/react-native-plugin)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 🏢 Enterprise Context

**Strata** is the Games & Procedural division of the [jbcom enterprise](https://jbcom.github.io). This plugin is part of a coherent suite of specialized tools, sharing a unified design system and interconnected with sibling organizations like [Agentic](https://agentic.dev) and [Extended Data](https://extendeddata.dev).

React Native plugin for [Strata 3D](https://strata.game) - cross-platform input, device detection, and haptics for mobile games.

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

- [@strata/core](https://strata.game) - Main library
- [@strata/capacitor-plugin](https://github.com/strata-game-library/capacitor-plugin) - Capacitor version
- [@strata/examples](https://github.com/strata-game-library/examples) - Example applications

## License

MIT
\n\n<!-- CI trigger -->
