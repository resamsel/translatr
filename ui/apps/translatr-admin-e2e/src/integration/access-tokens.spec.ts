import { test, expect } from '../support/test';
import { mockApi, waitForApi } from '../support/mock-api';
import { AccessTokensPage } from '../support/access-tokens-page.po';

test.describe('Admin Access Tokens', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/accesstokens*', 'access-tokens');
  });

  test('should list the access tokens returned by the API', async ({ page }) => {
    const tokens = await new AccessTokensPage(page).navigateTo();

    await expect(tokens.getPageName()).toHaveText('Access Tokens');
    await expect(tokens.getRows()).toHaveCount(2);
    await expect(tokens.getRows().first()).toContainText('CI token');
  });

  test('should persist an edit from the access token dialog', async ({ page }) => {
    await mockApi(page, '/api/accesstoken*', 'access-token-updated', { method: 'PUT' });
    const tokens = await new AccessTokensPage(page).navigateTo();

    await tokens.getEditButton('CI token').click();
    await tokens.getDialog().locator('input').first().fill('renamed');

    const update = waitForApi(page, '/api/accesstoken', 'PUT');
    await tokens.getDialog().locator('button[transloco="button.save"]').click();

    expect((await update).postDataJSON()).toMatchObject({ id: 1001, name: 'renamed' });
  });
});
