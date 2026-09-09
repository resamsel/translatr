import { test, expect } from '../../../support/test';
import { mockApi, remockApi } from '../../../support/mock-api';
import { UserAccessTokensPage } from '../../../support/user/user-access-tokens-page.po';

test.describe('User Access Tokens', () => {
  let tokensPage: UserAccessTokensPage;

  test.beforeEach(async ({ page }) => {
    tokensPage = new UserAccessTokensPage(page, 'johndoe');

    await mockApi(page, '/api/johndoe', 'johndoe');
    await mockApi(page, '/api/projects*', 'johndoe/projects');
    await mockApi(page, '/api/activities*', 'johndoe/activities');
    await mockApi(page, '/api/accesstokens*', 'johndoe/access-tokens');
  });

  test('should list the access tokens', async () => {
    // given

    // when
    await tokensPage.navigateTo();

    // then
    await expect(tokensPage.getRows()).toHaveCount(2);
    await expect(tokensPage.getRows().filter({ hasText: 'CI token' })).toHaveCount(1);
    await expect(tokensPage.getRows().filter({ hasText: 'Local dev' })).toHaveCount(1);
  });

  test('should show an empty view when there are none', async ({ page }) => {
    // given
    await remockApi(page, '/api/accesstokens*', 'johndoe/access-tokens-empty');

    // when
    await tokensPage.navigateTo();

    // then
    await expect(tokensPage.getEmptyView()).toBeVisible();
    await expect(tokensPage.getRows()).toHaveCount(0);
  });

  test('should navigate to the create page from the FAB', async ({ page }) => {
    // given

    // when
    await tokensPage.navigateTo();
    await tokensPage.getFloatingActionButton().click();

    // then
    await expect(page).toHaveURL(/\/johndoe\/access-tokens\/create/);
  });
});
