import { test, expect } from '../support/test';
import { mockApi } from '../support/mock-api';

test.describe('Debug project spec', () => {
  test('step 1: can visit project page', async ({ page }) => {
    await page.goto('johndoe/p1');
    await expect(page).toHaveURL(/johndoe/);
  });

  test('step 2: intercept then visit', async ({ page }) => {
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await page.goto('johndoe/p1');
    await expect(page).toHaveURL(/johndoe/);
  });

  test('step 3: project/star patterns', async ({ page }) => {
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    // PORT-NOTE: the Cypress test only asserted via `cy.log('registered intercept
    // successfully')`, which has no Playwright equivalent. Registering the route
    // without throwing is the check.
  });
});
