module.exports = {
  name: 'translatr-sdk',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/libs/translatr-sdk',
  reporters: [
    'default',
    ['jest-junit', { suiteName: 'translatr-sdk', outputDirectory: 'tmp/translatr-sdk' }]
  ]
};
