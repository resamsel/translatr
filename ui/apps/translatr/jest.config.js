module.exports = {
  name: 'translatr',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/apps/translatr',
  reporters: [
    'default',
    [
      'jest-junit',
      {
        suiteName: 'translatr-ui',
        outputDirectory: 'tmp/test-reports',
        outputName: 'translatr.xml'
      }
    ]
  ],
  snapshotSerializers: [
    'jest-preset-angular/build/serializers/no-ng-attributes',
    'jest-preset-angular/build/serializers/ng-snapshot',
    'jest-preset-angular/build/serializers/html-comment'
  ]
};
