import { test, expect } from '../support/test';
import { mockApi, remockApi } from '../support/mock-api';
import { UsersPage } from '../support/users-page.po';

test.describe('Admin Auth Guard', () => {
  test('should redirect a non-admin user to /forbidden', async ({ page }) => {
    await remockApi(page, '/api/me*', 'me-non-admin');

    await page.goto('users');

    await expect(page).toHaveURL(/\/forbidden/);
  });

  test('should let an admin user through to a guarded route', async ({ page }) => {
    await mockApi(page, '/api/user*', 'users');
    await mockApi(page, '/api/project*', 'projects');
    await mockApi(page, '/api/activities*', 'activities');

    await new UsersPage(page).navigateTo();

    await expect(page).toHaveURL(/\/users$/);
    await expect(page.locator('app-navbar .page')).toHaveText('Users');
  });
});
