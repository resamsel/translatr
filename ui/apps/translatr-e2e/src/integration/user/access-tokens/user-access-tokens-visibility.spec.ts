import { test, expect } from '../../../support/test';
import { mockApi, remockApi } from '../../../support/mock-api';
import { UserAccessTokensPage } from '../../../support/user/user-access-tokens-page.po';

test.describe('User Access Tokens Visibility', () => {
  test.beforeEach(async ({ page }) => {
    await remockApi(page, '/api/me*', 'janesmith');
    await mockApi(page, '/api/johndoe', 'johndoe');
    await mockApi(page, '/api/projects*', 'johndoe/projects');
    await mockApi(page, '/api/activities*', 'johndoe/activities');
    await mockApi(page, '/api/accesstokens*', 'johndoe/access-tokens');
  });

  test('should block another user from johndoe access-tokens (MyselfGuard)', async ({ page }) => {
    // given
    const tokensPage = new UserAccessTokensPage(page, 'johndoe');

    // when
    await tokensPage.navigateTo();

    // then — MyselfGuard cancels the navigation, the screen never renders
    await expect(page.locator('app-user-access-tokens')).toHaveCount(0);
    await expect(page.locator('.floating-action-btn')).toHaveCount(0);
  });
});
