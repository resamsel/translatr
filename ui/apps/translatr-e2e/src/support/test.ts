import { test as base, expect } from '@playwright/test';
import { mockApi } from './mock-api';

/**
 * Base test for the translatr e2e suite.
 *
 * Every test gets a fresh browser context (cookies already clean, so the old
 * `cy.clearCookies()` preamble is unnecessary) plus:
 *  - a default authenticated `/api/me*` response (override per-test with
 *    `mockUnauthenticated` / `remockApi`);
 *  - empty defaults for the two endpoints almost every screen hits on load
 *    (`/api/authclients`, `/api/activities/aggregated*`);
 *  - a catch-all that logs any unmocked `/api/*` call and replies 404, so a
 *    missing mock surfaces loudly instead of hanging on the real backend.
 *
 * The catch-all is registered first (lowest priority); the defaults and any
 * per-test `mockApi` calls are registered later and therefore win.
 */
export const test = base.extend({
  page: async ({ page }, use) => {
    await page.route(/\/api\//, async (route) => {
      console.warn(`[e2e] unmocked API call: ${route.request().method()} ${route.request().url()}`);
      // Abort rather than fulfill: an unmocked call must fail without an HTTP
      // status, so it never trips the AuthInterceptor's 401/403/404 redirects
      // (a real backend answered these in the Cypress suite).
      await route.abort();
    });

    await mockApi(page, '/api/me*', 'me');
    await mockApi(page, '/api/authclients', []);
    await mockApi(page, '/api/activities/aggregated*', []);

    await use(page);
  },
});

export { expect };
