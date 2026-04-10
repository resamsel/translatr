module.exports = {
  name: 'generator',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/libs/generator',
  reporters: [
    'default',
    [
      'jest-junit',
      { suiteName: 'generator', outputDirectory: 'tmp/test-reports', outputName: 'generator.xml' }
    ]
  ],
  snapshotSerializers: [
    'jest-preset-angular/build/serializers/ng-snapshot',
    'jest-preset-angular/build/serializers/html-comment'
  ]
};
