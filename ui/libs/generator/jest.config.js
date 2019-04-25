module.exports = {
  name: 'generator',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/libs/generator',
  snapshotSerializers: [
    'jest-preset-angular/AngularSnapshotSerializer.js',
    'jest-preset-angular/HTMLCommentSerializer.js'
  ]
};
