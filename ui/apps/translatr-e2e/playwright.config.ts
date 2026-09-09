import { defineConfig, devices } from '@playwright/test';
import { nxE2EPreset } from '@nx/playwright/preset';
import { workspaceRoot } from '@nx/devkit';

const port = 4210;
// The translatr app is built with `baseHref: /ui/`, so the dev server serves it
// under /ui/. baseURL carries that subpath; page objects navigate with paths
// relative to it (e.g. `page.goto('dashboard')`, not `'/dashboard'`).
const baseURL = process.env['BASE_URL'] ?? `http://localhost:${port}/ui/`;

/**
 * translatr end-to-end tests (Playwright).
 * Dev server is started via `webServer` below — the `@nx/playwright:playwright`
 * executor has no `devServerTarget` option.
 */
export default defineConfig({
  ...nxE2EPreset(__filename, { testDir: './src/integration' }),
  testMatch: '**/*.spec.ts',
  timeout: 30_000,
  expect: { timeout: 10_000 },
  fullyParallel: true,
  retries: process.env['CI'] ? 2 : 0,
  reporter: [
    ['list'],
    ['junit', { outputFile: 'tmp/test-reports/junit.xml' }],
    ['html', { open: 'never' }],
  ],
  use: {
    baseURL,
    trace: 'on-first-retry',
    video: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    command: `npx nx run translatr:serve --port ${port}`,
    url: baseURL,
    reuseExistingServer: !process.env['CI'],
    cwd: workspaceRoot,
    timeout: 180_000,
  },
});
