module.exports = {
  name: 'translatr-model',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/libs/translatr-model',
  reporters: [
    'default',
    [
      'jest-junit',
      {
        suiteName: 'translatr-model',
        outputDirectory: 'tmp/test-reports',
        outputName: 'translatr-model.xml'
      }
    ]
  ],
  snapshotSerializers: [
    'jest-preset-angular/build/serializers/ng-snapshot',
    'jest-preset-angular/build/serializers/html-comment'
  ]
};
