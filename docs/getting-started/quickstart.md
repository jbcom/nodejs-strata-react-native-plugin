# Quickstart

This guide will help you get started with `@jbcom/strata-react-native-plugin`.

## Basic Usage

### Using the Device Profile

```tsx
import { useDevice } from '@jbcom/strata-react-native-plugin';

function GameScreen() {
  const device = useDevice();

  return (
    <View>
      <Text>Platform: {device.platform}</Text>
      <Text>Orientation: {device.orientation}</Text>
      <Text>Performance Mode: {device.performanceMode}</Text>
    </View>
  );
}
```

### Handling Input

Wrap your game view with `StrataInputProvider` to capture touches:

```tsx
import { StrataInputProvider, InputSnapshot } from '@jbcom/strata-react-native-plugin';

function MyGame() {
  const handleInput = (snapshot: InputSnapshot) => {
    // Send input to your game engine (e.g., React Three Fiber)
    console.log('Touches:', snapshot.touches.length);
  };

  return (
    <StrataInputProvider onInput={handleInput}>
      <YourGameView />
    </StrataInputProvider>
  );
}
```

### Haptic Feedback

```tsx
import { useHaptics } from '@jbcom/strata-react-native-plugin';

function ActionButton() {
  const { trigger } = useHaptics();

  const onPress = async () => {
    await trigger({ intensity: 'medium' });
    // Perform action
  };

  return <Button title="Trigger" onPress={onPress} />;
}
```

### Orientation Control

```tsx
import { setOrientation } from '@jbcom/strata-react-native-plugin';

async function lockLandscape() {
  await setOrientation('landscape');
}
```

## Usage with React Three Fiber

The plugin is designed to work seamlessly with `@react-three/fiber`:

```tsx
import { Canvas } from '@react-three/fiber';
import { StrataInputProvider } from '@jbcom/strata-react-native-plugin';

export default function App() {
  return (
    <StrataInputProvider>
      <Canvas>
        <Your3DScene />
      </Canvas>
    </StrataInputProvider>
  );
}
```
