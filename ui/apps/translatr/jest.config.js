module.exports = {
  name: 'translatr',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/apps/translatr',
  reporters: [
    'default',
    ['jest-junit', { suiteName: 'translatr-ui', outputDirectory: 'tmp/translatr' }]
  ],
  snapshotSerializers: [
    // TODO: replace these commented serializers with the ones below after switching to Angular 9
    // 'jest-preset-angular/build/AngularNoNgAttributesSnapshotSerializer.js',
    // 'jest-preset-angular/build/AngularSnapshotSerializer.js',
    // 'jest-preset-angular/build/HTMLCommentSerializer.js'
    'jest-preset-angular/AngularNoNgAttributesSnapshotSerializer.js',
    'jest-preset-angular/AngularSnapshotSerializer.js',
    'jest-preset-angular/HTMLCommentSerializer.js'
  ]
};
