import { test, expect } from '../../support/test';
import { mockApi } from '../../support/mock-api';
import { UserPage } from '../../support/user/user-page.po';

test.describe('User', () => {
  let userPage: UserPage;

  test.beforeEach(async ({ page }) => {
    userPage = new UserPage(page, 'johndoe');

    await mockApi(page, '/api/johndoe', 'johndoe');
    await mockApi(page, '/api/projects*', 'johndoe/projects');
    await mockApi(page, '/api/activities*', 'johndoe/activities');
  });

  test('should show user page', async ({ page }) => {
    // given

    // when
    await userPage.navigateTo();

    // then
    await expect(page).toHaveTitle('John Doe - Translatr');
  });
});
