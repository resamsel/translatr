module.exports = {
  name: 'translatr-model',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/libs/translatr-model',
  snapshotSerializers: [
    'jest-preset-angular/AngularSnapshotSerializer.js',
    'jest-preset-angular/HTMLCommentSerializer.js'
  ]
};
