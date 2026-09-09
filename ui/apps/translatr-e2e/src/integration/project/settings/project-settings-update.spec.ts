import { test, expect } from '../../../support/test';
import { mockApi } from '../../../support/mock-api';
import { ProjectPage } from '../../../support/project/project-page.po';
import { ProjectSettingsPage } from '../../../support/project/project-settings-page.po';

test.describe('Project Settings Update', () => {
  let settingsPage: ProjectSettingsPage;

  test.beforeEach(async ({ page }) => {
    settingsPage = new ProjectSettingsPage(page, 'johndoe', 'p1');

    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
    await mockApi(page, '/api/activities*', 'johndoe/p1/activities');
  });

  test('should persist on save', async ({ page }) => {
    // given
    await mockApi(page, '/api/project', 'johndoe/p2', { method: 'PUT' });
    await mockApi(page, '/api/johndoe/p2*', 'johndoe/p2');

    // when
    await settingsPage.navigateTo();

    // then
    await settingsPage.getNameField().fill('p2');
    await expect(settingsPage.getNameField()).toHaveValue('p2');
    await settingsPage.getDescriptionField().fill('p2d');
    await expect(settingsPage.getDescriptionField()).toHaveValue('p2d');
    await settingsPage.getSaveButton().click();

    // cy.url().should('contain', 'johndoe/p2/settings');

    const projectPage: ProjectPage = await new ProjectPage(page, 'johndoe', 'p2').navigateTo();

    await expect(projectPage.getDescription()).toHaveText('p2d');
  });

  test('should persist with 255 chars name on save', async ({ page }) => {
    // given
    const name = `2${'p2'.repeat(127)}`;
    await mockApi(page, '/api/project', 'johndoe/p2-name-255', { method: 'PUT' });
    await mockApi(page, `/api/johndoe/${name}*`, 'johndoe/p2-name-255');

    // when
    await settingsPage.navigateTo();

    // then
    await settingsPage.getNameField().fill(name);
    await expect(settingsPage.getNameField()).toHaveValue(name);
    await settingsPage.getSaveButton().click();

    await expect(settingsPage.getPageName()).toHaveText(`johndoe/${name}`);
    // cy.url().should('contain', `johndoe/${name}/settings`);
  });

  test('should not persist when name not unique', async ({ page }) => {
    // given
    await mockApi(page, '/api/project', 'johndoe/p2-not-unique', { method: 'PUT', status: 400 });

    // when
    await settingsPage.navigateTo();

    // then
    await settingsPage.getNameField().fill('p2');
    await expect(settingsPage.getNameField()).toHaveValue('p2');
    await settingsPage.getSaveButton().click();

    await expect(settingsPage.getNameFieldError()).toBeVisible();
    await expect(page).toHaveURL(/johndoe\/p1\/settings/);
  });

  test('should not persist if name does not match pattern', async () => {
    // given

    // when
    await settingsPage.navigateTo();

    // then
    await settingsPage.getNameField().fill('p2 d2');
    await expect(settingsPage.getNameField()).toHaveValue('p2 d2');
    await settingsPage.getNameField().blur();
    await expect(settingsPage.getSaveButton()).toBeDisabled();

    await expect(settingsPage.getNameFieldError()).toBeVisible();
  });

  test('should not persist if name is too long', async () => {
    // given

    // when
    await settingsPage.navigateTo();

    // then
    await settingsPage.getNameField().fill('p2'.repeat(128));
    await expect(settingsPage.getNameField()).toHaveValue('p2'.repeat(128));
    await expect(settingsPage.getSaveButton()).toBeDisabled();
  });

  test('should not persist if description is too long', async () => {
    // given
    const name = 'p2d '.repeat(501);

    // when
    await settingsPage.navigateTo();

    // then
    await settingsPage.getDescriptionField().fill(name);
    await expect(settingsPage.getDescriptionField()).toHaveValue(name);
    await expect(settingsPage.getSaveButton()).toBeDisabled();
  });
});
