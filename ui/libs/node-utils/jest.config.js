module.exports = {
  name: 'node-utils',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/libs/node-utils',
  reporters: [
    'default',
    [
      'jest-junit',
      { suiteName: 'node-utils', outputDirectory: 'tmp/test-reports', outputName: 'node-utils.xml' }
    ]
  ]
};
