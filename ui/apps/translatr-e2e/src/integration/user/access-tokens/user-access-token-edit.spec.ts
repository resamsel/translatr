import { test, expect } from '../../../support/test';
import { mockApi, remockApi, waitForApi } from '../../../support/mock-api';
import { UserAccessTokensPage } from '../../../support/user/user-access-tokens-page.po';
import { UserAccessTokenPage } from '../../../support/user/user-access-token-page.po';

test.describe('User Access Token Edit', () => {
  const tokenId = 'a1000000-0000-0000-0000-000000000001';

  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/johndoe', 'johndoe');
    await mockApi(page, '/api/projects*', 'johndoe/projects');
    await mockApi(page, '/api/activities*', 'johndoe/activities');
    await mockApi(page, '/api/accesstokens*', 'johndoe/access-tokens');
    await mockApi(page, '/api/accesstoken/*', 'johndoe/access-token');
  });

  test('should populate the fields from the token', async ({ page }) => {
    // given
    const tokenPage = await new UserAccessTokenPage(page, 'johndoe', tokenId).navigateTo();

    // when

    // then
    await expect(tokenPage.getNameField()).toHaveValue('CI token');
  });

  test('should persist on save and confirm with a snackbar', async ({ page }) => {
    // given
    await mockApi(page, '/api/accesstoken', {}, { method: 'PUT' });
    const tokenPage = await new UserAccessTokenPage(page, 'johndoe', tokenId).navigateTo();

    // when
    const updateAccessToken = waitForApi(page, '/api/accesstoken', 'PUT');
    await tokenPage.getNameField().fill('CI token renamed');
    await tokenPage.getSaveButton().click();

    // then
    await updateAccessToken;
    await expect(page.locator('.mat-mdc-snack-bar-label').first()).toBeVisible();
    await expect(page).toHaveURL(/\/johndoe\/access-tokens/);
  });

  test('should delete a token from the list and remove its row', async ({ page }) => {
    // given
    const listPage = await new UserAccessTokensPage(page, 'johndoe').navigateTo();
    await expect(listPage.getRows()).toHaveCount(2);
    await mockApi(page, '/api/accesstoken/*', 'johndoe/access-token', { method: 'DELETE' });
    await remockApi(page, '/api/accesstokens*', 'johndoe/access-tokens-minus-one');

    // when
    const deleteAccessToken = waitForApi(page, '/api/accesstoken/*', 'DELETE');
    await listPage
      .getRows()
      .filter({ hasText: 'CI token' })
      .locator('confirm-button.delete button')
      .click();
    await page.locator('.mat-mdc-menu-panel button.confirm').click();

    // then
    await deleteAccessToken;
    await expect(listPage.getRows()).toHaveCount(1);
    await expect(listPage.getRows().filter({ hasText: 'CI token' })).toHaveCount(0);
  });
});
