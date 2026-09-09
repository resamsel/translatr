import { test, expect } from '../../../support/test';
import { mockApi, remockApi } from '../../../support/mock-api';
import { UserSettingsPage } from '../../../support/user/user-settings-page.po';

test.describe('User Settings Update', () => {
  let settingsPage: UserSettingsPage;

  test.beforeEach(async ({ page }) => {
    settingsPage = new UserSettingsPage(page, 'johndoe');

    await remockApi(page, '/api/me*', 'johndoe');
    await mockApi(page, '/api/johndoe', 'johndoe');
    await mockApi(page, '/api/projects*', 'johndoe/projects');
    await mockApi(page, '/api/activities*', 'johndoe/activities');
  });

  test('should persist on save', async ({ page }) => {
    // given
    await mockApi(page, '/api/user', 'johndoe2', { method: 'PUT' });
    await mockApi(page, '/api/johndoe2', 'johndoe2');

    // when
    await settingsPage.navigateTo();
    await settingsPage.getNameField().fill('John Doe2');
    await settingsPage.getUsernameField().fill('johndoe2');
    await settingsPage.getSaveButton().click();

    // then
    await expect(page).toHaveURL(/johndoe2\/settings/);
  });

  test('should persist with 32 chars name on save', async ({ page }) => {
    // given
    const name = `${'John Doe'.repeat(4)}`;
    await mockApi(page, '/api/user', 'johndoe-name-32', { method: 'PUT' });

    // when
    await settingsPage.navigateTo();
    await settingsPage.getNameField().fill(name);
    await settingsPage.getSaveButton().click();

    // then
    await expect(page).toHaveURL(/johndoe\/settings/);
  });

  test('should not persist when name not unique', async ({ page }) => {
    // given
    await mockApi(page, '/api/user', 'johndoe-not-unique', { method: 'PUT', status: 400 });

    // when
    await settingsPage.navigateTo();
    await settingsPage.getNameField().fill('janesmith');

    // then
    await settingsPage.getSaveButton().click();

    await expect(settingsPage.getNameFieldError()).toBeVisible();
    await expect(page).toHaveURL(/johndoe\/settings/);
  });

  test('should not persist if username does not match pattern', async () => {
    // given

    // when
    await settingsPage.navigateTo();
    await settingsPage.getUsernameField().fill('john doe');
    await settingsPage.getUsernameField().blur();

    // then
    await expect(settingsPage.getSaveButton()).toBeDisabled();

    await expect(settingsPage.getUsernameFieldError()).toBeVisible();
  });

  test('should not persist if name is too long', async () => {
    // given

    // when
    await settingsPage.navigateTo();
    await settingsPage.getNameField().fill(`${'John Doe'.repeat(4)}J`);

    // then
    await expect(settingsPage.getSaveButton()).toBeDisabled();
  });

  test('should not persist if username is too long', async () => {
    // given
    const name = `${'johndoe'.repeat(4)}johnd`;

    // when
    await settingsPage.navigateTo();

    // then
    await settingsPage.getUsernameField().fill(name);
    await expect(settingsPage.getSaveButton()).toBeDisabled();
  });
});
