const { defineConfig } = require('cypress');

module.exports = defineConfig({
  e2e: {
    fileServerFolder: '.',
    fixturesFolder: './src/fixtures',
    specPattern: './src/integration/**/*.spec.ts',
    supportFile: false,
    video: true,
    videosFolder: '../../dist/cypress/apps/translatr-admin-e2e/videos',
    screenshotsFolder: '../../dist/cypress/apps/translatr-admin-e2e/screenshots',
    chromeWebSecurity: false,
    projectId: 'hhbnaa',
    reporter: 'junit',
    reporterOptions: {
      mochaFile: 'tmp/test-reports/junit.xml',
    },
  },
});
