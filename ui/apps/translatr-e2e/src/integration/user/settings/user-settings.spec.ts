import { test, expect } from '../../../support/test';
import { mockApi } from '../../../support/mock-api';
import { UserSettingsPage } from '../../../support/user/user-settings-page.po';

test.describe('User Settings', () => {
  let settingsPage: UserSettingsPage;

  test.beforeEach(async ({ page }) => {
    settingsPage = new UserSettingsPage(page, 'johndoe');

    await mockApi(page, '/api/johndoe', 'johndoe');
    await mockApi(page, '/api/projects*', 'johndoe/projects');
    await mockApi(page, '/api/activities*', 'johndoe/activities');
  });

  test('should show user settings', async () => {
    // given

    // when
    await settingsPage.navigateTo();

    // then
    await expect(settingsPage.getPageName()).toHaveText('John Doe');
  });

  test('should have name and username set', async () => {
    // given

    // when
    await settingsPage.navigateTo();

    // then
    await expect(settingsPage.getNameField()).toHaveValue('John Doe');
    await expect(settingsPage.getUsernameField()).toHaveValue('johndoe');
  });
});
