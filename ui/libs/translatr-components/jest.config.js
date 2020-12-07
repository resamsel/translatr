module.exports = {
  name: 'translatr-components',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/libs/translatr-components',
  reporters: [
    'default',
    [
      'jest-junit',
      {
        suiteName: 'translatr-components',
        outputDirectory: 'tmp/test-reports',
        outputName: 'translatr-components.xml'
      }
    ]
  ]
};
