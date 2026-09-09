import { test, expect } from '../../../support/test';
import { mockApi } from '../../../support/mock-api';
import { ProjectSettingsPage } from '../../../support/project/project-settings-page.po';

test.describe('Project Settings', () => {
  let settingsPage: ProjectSettingsPage;

  test.beforeEach(async ({ page }) => {
    settingsPage = new ProjectSettingsPage(page, 'johndoe', 'p1');

    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
    await mockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
  });

  test('should have page name Project Settings', async () => {
    // given

    // when
    await settingsPage.navigateTo();

    // then
    await expect(settingsPage.getPageName()).toHaveText('johndoe/p1');
  });

  test('should have name and description set', async () => {
    // given

    // when
    await settingsPage.navigateTo();

    // then
    await expect(settingsPage.getNameField()).toHaveValue('p1');
    await expect(settingsPage.getDescriptionField()).toHaveValue('p1d');
  });
});
