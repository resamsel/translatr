module.exports = {
  name: 'utils',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/libs/utils',
  reporters: [
    'default',
    [
      'jest-junit',
      { suiteName: 'utils', outputDirectory: 'tmp/test-reports', outputName: 'utils.xml' }
    ]
  ]
};
