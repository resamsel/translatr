module.exports = {
  name: 'translatr-admin',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/apps/translatr-admin/',
  snapshotSerializers: [
    'jest-preset-angular/AngularSnapshotSerializer.js',
    'jest-preset-angular/HTMLCommentSerializer.js'
  ]
};
