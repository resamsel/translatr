module.exports = {
  name: 'generator',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/libs/generator',
  reporters: [
    'default',
    ['jest-junit', { suiteName: 'generator', outputDirectory: 'tmp/generator' }]
  ],
  snapshotSerializers: [
    'jest-preset-angular/AngularSnapshotSerializer.js',
    'jest-preset-angular/HTMLCommentSerializer.js'
  ]
};
