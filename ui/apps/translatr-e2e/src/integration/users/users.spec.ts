import { test, expect } from '../../support/test';
import { mockApi, waitForApi } from '../../support/mock-api';
import { UsersPage } from '../../support/users-page.po';

test.describe('Users', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/user*', 'users');
  });

  test('should list users', async ({ page }) => {
    // given

    // when
    const users = await new UsersPage(page).navigateTo();

    // then
    await expect(users.getRows()).toHaveCount(3);
  });

  test('should filter on search input', async ({ page }) => {
    // given
    await mockApi(page, '/api/user*search=*', 'users-search');

    // when
    const users = await new UsersPage(page).navigateTo();
    const search = waitForApi(page, '/api/user*search=*', 'GET');
    await users.getSearchField().pressSequentially('jane');
    await page.locator('mat-option').first().click();

    // then
    expect((await search).url()).toContain('search=jane');
    await expect(users.getRows()).toHaveCount(1);
  });

  test('should load more users', async ({ page }) => {
    // given
    await mockApi(page, '/api/user*limit=16*', 'users-page2');

    // when
    const users = await new UsersPage(page).navigateTo();
    await expect(users.getRows()).toHaveCount(3);
    const more = waitForApi(page, '/api/user*limit=16*', 'GET');
    await users.getLoadMoreButton().click();

    // then
    expect((await more).url()).toContain('limit=16');
    await expect(users.getRows()).toHaveCount(3);
    await expect(users.getRows().first()).toContainText('Ronny Lee');
  });

  test('should show no user rows when there are no users', async ({ page }) => {
    // given
    await mockApi(page, '/api/user*', 'users-empty');

    // when
    const users = await new UsersPage(page).navigateTo();

    // then
    await expect(page.locator('app-user-list mat-nav-list')).toHaveCount(1);
    await expect(users.getRows()).toHaveCount(0);
  });

  test('should navigate to a user page on row click', async ({ page }) => {
    // given
    await mockApi(page, '/api/johndoe', 'johndoe');
    await mockApi(page, '/api/projects*', 'johndoe/projects');
    await mockApi(page, '/api/activities*', 'johndoe/activities');

    // when
    const users = await new UsersPage(page).navigateTo();
    await users.getRows().first().click();

    // then
    await expect(page).toHaveURL(/\/johndoe$/);
  });
});
