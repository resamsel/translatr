module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  // testMatch: ['**/+(*.)+(spec|test).+(ts|js)?(x)'],
  transform: {
    '^.+\\.(ts)$': 'ts-jest',
    '^.+\\.(ts|js|html)$': 'jest-preset-angular',
    '.+\\.(svg)$': 'jest-transform-stub'
  },
  resolver: '@nrwl/jest/plugins/resolver',
  moduleFileExtensions: ['ts', 'js', 'html'],
  coverageReporters: ['html', 'lcov', 'text'],
  setupFilesAfterEnv: ['<rootDir>/src/test-setup.ts'],
  globalSetup: 'jest-preset-angular/global-setup'
};
