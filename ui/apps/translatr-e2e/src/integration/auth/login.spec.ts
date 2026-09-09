import { test, expect } from '../../support/test';
import { mockApi, mockUnauthenticated } from '../../support/mock-api';
import { LoginPage } from '../../support/auth/login-page.po';

const authClients = [
  { key: 'keycloak', url: '/login/keycloak' },
  { key: 'google', url: '/login/google' },
];

test.describe('Login', () => {
  test.beforeEach(async ({ page }) => {
    await mockUnauthenticated(page);
    await mockApi(page, '/api/authclients', authClients);
  });

  test('should render the login page with provider links', async ({ page }) => {
    const login = await new LoginPage(page).navigateTo();

    await expect(login.getPageName()).toHaveText('Sign in');
    await expect(login.getProviderLinks()).toHaveCount(2);
    await expect(page.locator('a.client-keycloak')).toHaveCount(1);
  });

  test('should redirect an unauthenticated visit to /dashboard to /login', async ({ page }) => {
    await page.goto('dashboard');

    await expect(page).toHaveURL(/\/login/);
  });
});
