import { test, expect } from '../../../support/test';
import { mockApi, remockApi, waitForApi } from '../../../support/mock-api';
import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

test.describe('Project Locales Add Locale', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
    await remockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
  });

  test('should show locale add button', async ({ page }) => {
    // given

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();

    // then
    await expect(locales.getFloatingActionButton()).toBeVisible();
  });

  test('should show locale add dialog on clicking add button', async ({ page }) => {
    // given

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();
    await locales.getFloatingActionButton().click();

    // then
    await expect(locales.getDialog()).toBeVisible();
    await expect(locales.getDialog().locator('mat-form-field.name input')).toHaveValue('');
    await expect(locales.getDialog().locator('[mat-dialog-title]')).toHaveText('Add Language');
  });

  test('should hide locale add dialog on clicking cancel button', async ({ page }) => {
    // given

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();
    await locales.getFloatingActionButton().click();
    await locales.getDialog().locator('button.cancel').click();

    // then
    await expect(locales.getDialog()).toHaveCount(0);
  });

  test('should add the locale and open the new language on save', async ({ page }) => {
    // given
    await mockApi(page, '/api/locale', 'johndoe/p1/locale-created', { method: 'POST' });
    await remockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales-added');
    await mockApi(page, '/api/johndoe/p1/locales/fr', 'johndoe/p1/locale-created');

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();
    await locales.getFloatingActionButton().click();
    await locales.getDialog().locator('mat-form-field.name input').fill('fr');

    const createLocale = waitForApi(page, '/api/locale', 'POST');
    await locales.getDialog().locator('button[transloco="button.save"], button.save').click();

    // then
    expect((await createLocale).postDataJSON()).toMatchObject({ name: 'fr' });
    await expect(locales.getDialog()).toHaveCount(0);
    await expect(page).toHaveURL(/\/johndoe\/p1\/locales\/fr$/);
  });
});
