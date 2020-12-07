module.exports = {
  name: 'lets-generate',
  preset: '../../jest.config.js',
  coverageDirectory: '../../coverage/apps/lets-generate',
  reporters: [
    'default',
    ['jest-junit', { suiteName: 'lets-generate', outputDirectory: 'tmp/lets-generate' }]
  ]
};
