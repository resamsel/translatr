import { test, expect } from '../../../support/test';
import { mockApi, remockApi } from '../../../support/mock-api';
import { KeyEditorPage } from '../../../support/project/key-editor-page.po';

test.describe('Project Key Editor Empty State', () => {
  let editor: KeyEditorPage;

  test.beforeEach(async ({ page }) => {
    editor = new KeyEditorPage(page, 'johndoe', 'p0', 'k1');

    await mockApi(page, '/api/johndoe/p0*', 'johndoe/p0');
    await mockApi(page, '/api/johndoe/p0/keys/k1', 'johndoe/p0/keys/k1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p0/locales');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p0/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p0/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p0/activities');
  });

  test('should have no languages in sidebar', async () => {
    // given

    // when
    await editor.navigateTo();

    // then
    await expect(editor.getNavListItems()).toHaveCount(0);
  });

  test('should show language creation teaser in sidebar', async ({ page }) => {
    // given

    // when
    await editor.navigateTo();

    // then
    await expect(page.locator('dev-empty-view [data-test="locale-teaser"]')).toHaveText(
      'It looks like there are no languages defined, yet - would you like to add one now?',
    );
    await expect(page.locator('dev-empty-view [data-test="create-locale"]')).toHaveText('Add language');
  });

  test('should show language creation dialog when clicking button', async ({ page }) => {
    // given

    // when
    await editor.navigateTo();
    await page.locator('dev-empty-view [data-test="create-locale"]').click();

    // then
    await expect(page.locator('mat-dialog-container')).toBeAttached();
  });

  test('should show language creation dialog when clicking button (creates locale)', async ({ page }) => {
    // PORT-NOTE: the Cypress spec has two `it` blocks with the identical title
    // "should show language creation dialog when clicking button"; kept both,
    // disambiguating this (second) title so Playwright does not silently collide.

    // given

    // when
    await editor.navigateTo();
    await page.locator('dev-empty-view [data-test="create-locale"]').click();
    await page.locator('mat-dialog-container input').fill('de');
    await mockApi(page, '/api/locale', 'johndoe/p0/locale-created', { method: 'POST' });
    await remockApi(page, '/api/project/*/locales*', 'johndoe/p0/locales-added');
    await page.locator('mat-dialog-container button.save').click();

    // then
    const active = editor.getNavList().locator('a.active');
    await expect(active).toHaveCount(1);
    await expect(active.locator('h3')).toHaveText('German');
  });
});
