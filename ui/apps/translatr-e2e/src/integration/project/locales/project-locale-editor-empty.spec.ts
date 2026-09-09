import { test, expect } from '../../../support/test';
import { mockApi, remockApi } from '../../../support/mock-api';
import { LocaleEditorPage } from '../../../support/project/locale-editor-page.po';

test.describe('Project Locale Editor Empty State', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/johndoe/p3*', 'johndoe/p3');
    await mockApi(page, '/api/johndoe/p3/locales/default', 'johndoe/p3/locales/default');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p3/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p3/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p3/messages');
  });

  test('should have no keys in sidebar', async ({ page }) => {
    // given

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p3', 'default').navigateTo();

    // then
    await expect(editor.getNavListItems()).toHaveCount(0);
  });

  test('should show key creation teaser in sidebar', async ({ page }) => {
    // given

    // when
    await new LocaleEditorPage(page, 'johndoe', 'p3', 'default').navigateTo();

    // then
    await expect(page.locator('dev-empty-view [data-test="key-teaser"]')).toHaveText(
      'It looks like there are no keys defined, yet - would you like to add one now?',
    );
    await expect(page.locator('dev-empty-view [data-test="create-key"]')).toHaveText('Add key');
  });

  test('should show key creation dialog when clicking button', async ({ page }) => {
    // given

    // when
    await new LocaleEditorPage(page, 'johndoe', 'p3', 'default').navigateTo();
    await page.locator('dev-empty-view [data-test="create-key"]').click();

    // then
    await expect(page.locator('mat-dialog-container')).toHaveCount(1);
  });

  // PORT-NOTE: the Cypress file had two `it()`s with the identical title
  // "should show key creation dialog when clicking button". Playwright rejects
  // duplicate test titles in one file, so this second one is suffixed. Body is
  // ported unchanged.
  test('should show key creation dialog when clicking button (create key on save)', async ({
    page,
  }) => {
    // given

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p3', 'default').navigateTo();
    await page.locator('dev-empty-view [data-test="create-key"]').click();
    await page.locator('mat-dialog-container input').fill('de');
    await mockApi(page, '/api/key', 'johndoe/p3/key-created', { method: 'POST' });
    await remockApi(page, '/api/project/*/keys*', 'johndoe/p3/keys-added');
    await page.locator('mat-dialog-container button.save').click();

    // then
    await expect(editor.getNavList().locator('a.active')).toHaveCount(1);
    await expect(editor.getNavList().locator('a.active h3')).toHaveText('k1');
  });
});
