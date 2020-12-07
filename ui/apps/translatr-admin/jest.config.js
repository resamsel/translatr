module.exports = {
  name: 'translatr-admin',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/apps/translatr-admin/',
  reporters: [
    'default',
    [
      'jest-junit',
      {
        suiteName: 'translatr-admin',
        outputDirectory: 'tmp/test-reports',
        outputName: 'translatr-admin.xml'
      }
    ]
  ],
  snapshotSerializers: [
    'jest-preset-angular/AngularSnapshotSerializer.js',
    'jest-preset-angular/HTMLCommentSerializer.js'
  ]
};
