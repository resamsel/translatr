import { test, expect } from '../../../support/test';
import { mockApi, waitForApi } from '../../../support/mock-api';
import { UserAccessTokenPage } from '../../../support/user/user-access-token-page.po';

test.describe('User Access Token Create', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/johndoe', 'johndoe');
    await mockApi(page, '/api/projects*', 'johndoe/projects');
    await mockApi(page, '/api/activities*', 'johndoe/activities');
    await mockApi(page, '/api/accesstokens*', 'johndoe/access-tokens');
  });

  test('should create a token and reveal the secret once', async ({ page }) => {
    // given
    await mockApi(page, '/api/accesstoken', 'johndoe/access-token-created', { method: 'POST' });
    const tokenPage = await new UserAccessTokenPage(page, 'johndoe', 'create').navigateTo();

    // when
    const createAccessToken = waitForApi(page, '/api/accesstoken', 'POST');
    await tokenPage.getNameField().fill('New token');
    await tokenPage.getSaveButton().click();

    // then
    await createAccessToken;
    await expect(tokenPage.getSecret()).toHaveValue('tk_live_ONE_TIME_SECRET_abcdef123456');
  });

  test('should confirm the save with a snackbar and stay on the form', async ({ page }) => {
    // given
    await mockApi(page, '/api/accesstoken', 'johndoe/access-token-created', { method: 'POST' });
    const tokenPage = await new UserAccessTokenPage(page, 'johndoe', 'create').navigateTo();

    // when
    const createAccessToken = waitForApi(page, '/api/accesstoken', 'POST');
    await tokenPage.getNameField().fill('New token');
    await tokenPage.getSaveButton().click();

    // then
    await createAccessToken;
    await expect(page.locator('.mat-mdc-snack-bar-label').first()).toContainText('has been saved');
    await expect(page).toHaveURL(/\/johndoe\/access-tokens\/create$/);
  });
});
