module.exports = {
  preset: 'react-native',
  moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx', 'json', 'node'],
  transform: {
    '^.+\\.(js|jsx|ts|tsx)$': ['babel-jest', { presets: ['module:@react-native/babel-preset'] }],
  },
  transformIgnorePatterns: [
    'node_modules/(?!(react-native|@react-native|@testing-library)/)',
  ],
  testMatch: ['**/tests/**/*.test.(ts|tsx)', '**/src/**/*.test.(ts|tsx)'],
  testEnvironment: 'node',
};
