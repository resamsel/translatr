import { test, expect } from '../../../support/test';
import { mockApi } from '../../../support/mock-api';
import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

test.describe('Project Locales Edit Locale', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
    await mockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
  });

  test('should show locale edit button', async ({ page }) => {
    // given

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();

    // then
    await expect(locales.getLocaleList().locator('button.edit').first()).toBeVisible();
  });

  test('should show locale edit dialog on clicking edit button', async ({ page }) => {
    // given

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();
    await locales.getLocaleList().locator('button.edit').first().click();

    // then
    await expect(locales.getDialog()).toBeVisible();
    await expect(locales.getDialog().locator('[mat-dialog-title]')).toHaveText('Edit Language');
    await expect(locales.getDialog().locator('mat-form-field.name input')).toHaveValue('de');
  });

  test('should hide locale edit dialog on clicking cancel button', async ({ page }) => {
    // given

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();
    await locales.getLocaleList().locator('button.edit').first().click();
    await locales.getDialog().locator('button.cancel').click();

    // then
    await expect(locales.getDialog()).toHaveCount(0);
  });
});
