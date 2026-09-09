import { test, expect } from '../support/test';
import { mockApi, waitForApi } from '../support/mock-api';
import { UsersPage } from '../support/users-page.po';

test.describe('Admin Users', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/user*', 'users');
    await mockApi(page, '/api/user/*', 'user');
    await mockApi(page, '/api/project*', 'projects');
    await mockApi(page, '/api/activities*', 'activities');
  });

  test('should list the users returned by the API', async ({ page }) => {
    const users = await new UsersPage(page).navigateTo();

    await expect(users.getRows()).toHaveCount(3);
    await expect(users.getRow('Jane Smith')).toContainText('janesmith');
  });

  test('should push the search term into the users request', async ({ page }) => {
    await mockApi(page, '/api/user*search=*', 'users');
    const users = await new UsersPage(page).navigateTo();

    const search = waitForApi(page, '/api/user*search=*');
    await users.getSearchField().fill('jane');
    await page.locator('mat-option').first().click();

    expect((await search).url()).toContain('search=jane');
  });

  test('should navigate to the user detail page on row click', async ({ page }) => {
    const users = await new UsersPage(page).navigateTo();

    await users.getRows().first().locator('a.link').click();

    await expect(page).toHaveURL(/\/users\/[0-9a-f-]+$/);
    await expect(users.getPageName()).toHaveText('Jane Smith');
  });

  test('should persist a role change from the user edit dialog', async ({ page }) => {
    await mockApi(page, '/api/user*', 'user-updated', { method: 'PUT' });
    const users = await new UsersPage(page).navigateTo();

    await users.getEditButton('Jane Smith').click();
    const roleSelect = users.getDialog().locator('mat-select[formControlName="role"]');
    await expect(roleSelect).toContainText('Admin');
    await roleSelect.click();
    await page.getByRole('option', { name: 'User', exact: true }).click();

    const update = waitForApi(page, '/api/user*', 'PUT');
    await users.getDialog().locator('button[transloco="button.save"]').click();

    expect((await update).postDataJSON()).toMatchObject({
      id: '5e15a05d-c583-45a0-84fa-1e770b2a4532',
      role: 'User',
    });
  });
});
