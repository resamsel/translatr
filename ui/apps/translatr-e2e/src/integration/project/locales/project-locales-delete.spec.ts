import { test, expect } from '../../../support/test';
import { mockApi } from '../../../support/mock-api';
import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

test.describe('Project Locales Delete Locale', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
    await mockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
  });

  test('should show locale delete button', async ({ page }) => {
    // given

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();

    // then
    await expect(locales.getLocaleList().locator('confirm-button.delete').first()).toBeVisible();
  });

  test('should show locale delete menu on clicking delete button', async ({ page }) => {
    // given

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();
    await locales.getLocaleList().locator('confirm-button.delete').first().click();

    // then
    await expect(page.locator('.mat-mdc-menu-panel button.confirm')).toHaveText('Remove');
  });

  test('should delete locale clicking delete button', async ({ page }) => {
    // given
    await mockApi(page, '/api/locale/*', 'johndoe/p1/locales/default', { method: 'DELETE' });

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();
    await locales.getLocaleList().locator('confirm-button.delete').first().click();
    await page.locator('.mat-mdc-menu-panel button.confirm').click();

    // then
    await expect(locales.getLocaleList().locator('a[mat-list-item]')).toHaveCount(1);
  });
});
