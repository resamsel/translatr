module.exports = {
  name: 'translatr',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/apps/translatr',
  snapshotSerializers: [
    'jest-preset-angular/build/AngularSnapshotSerializer.js',
    'jest-preset-angular/build/HTMLCommentSerializer.js'
  ]
};
