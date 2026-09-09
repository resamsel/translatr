import { test, expect } from '../../../support/test';
import { mockApi, remockApi, waitForApi } from '../../../support/mock-api';
import { KeyEditorPage } from '../../../support/project/key-editor-page.po';

test.describe('Project Key Editor', () => {
  let editor: KeyEditorPage;

  test.beforeEach(async ({ page }) => {
    editor = new KeyEditorPage(page, 'johndoe', 'p1', 'k1');

    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/johndoe/p1/keys/k1', 'johndoe/p1/keys/k1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/locales?*missing=true*', 'johndoe/p1/locales-missing');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
  });

  test('should have page title Key Editor', async () => {
    // given

    // when
    await editor.navigateTo();

    // then
    await expect(editor.getPageTitle()).toHaveText('Key Editor');
  });

  test('should have key k1 selected in sidebar', async () => {
    // given

    // when
    await editor.navigateTo();

    // then
    await expect(editor.getSelectedKeyField()).toHaveValue('k1');
  });

  test('should have two locales in sidebar', async () => {
    // given

    // when
    await editor.navigateTo();

    // then
    await expect(editor.getNavList().locator('a.locale')).toHaveCount(2);
  });

  test('should have no locale selected in sidebar', async () => {
    // given

    // when
    await editor.navigateTo();

    // then
    await expect(editor.getNavList().locator('a.active')).toHaveCount(0);
  });

  test('should have locale selected when activated in sidebar', async () => {
    // given

    // when
    await editor.navigateTo();

    // then
    await expect(editor.getNavList().locator('a.locale:first-of-type')).not.toHaveClass(/\bactive\b/);
    const firstLocale = editor.getNavList().locator('a.locale').first();
    await firstLocale.click();
    await expect(firstLocale).toHaveClass(/\bactive\b/);
  });

  test('should show editor when locale activated in sidebar', async () => {
    // given

    // when
    await editor.navigateTo();

    // then
    await editor.getNavList().locator('a.locale').first().click();

    await expect(editor.getEditor()).toBeVisible();
  });

  test('should show translation when locale activated in sidebar', async () => {
    // given

    // when
    await editor.navigateTo();
    await editor.getNavList().locator('a.locale').first().click();

    // then
    await expect(editor.getEditorContents()).toHaveText('Schlüssel 1');
  });

  test('should show meta when locale activated in sidebar', async () => {
    // given

    // when
    await editor.navigateTo();

    // then
    await editor.getNavList().locator('a.locale').first().click();

    await expect(editor.getMeta()).toBeVisible();
  });

  test('should show preview when locale activated in sidebar', async () => {
    // given

    // when
    await editor.navigateTo();

    // then
    await editor.getNavList().locator('a.locale').first().click();

    await expect(editor.getPreviewContents()).toHaveText('Schlüssel 1');
  });

  test('should show existing translations when translations tab selected', async () => {
    // given

    // when
    await editor.navigateTo();

    // then
    await editor.getNavList().locator('a.locale').first().click();

    await editor.getTranslationsTab().click();
    await expect(editor.getTranslationsBody().locator('mat-card')).toHaveCount(2);
  });

  test('should use translation when use translation is clicked', async ({ page }) => {
    // given

    // when
    await editor.navigateTo();

    // then
    await editor.getNavList().locator('a.locale').first().click();

    await editor.getTranslationsTab().click();
    await page.locator('.meta [role="tabpanel"] mat-card button.use-value').last().click();

    await expect(editor.getEditorContents()).toHaveText('Key One');
  });

  test('should only show locales with missing translations when filtered by those', async ({
    page,
  }) => {
    // given
    await mockApi(page, '/api/project/*/messages?*localeIds=*', 'johndoe/p1/messages-missing');

    // when
    await editor.navigateTo();

    await editor.getFilterField().focus();
    await page.locator('.autocomplete-option').first().click();

    // then
    await expect(page.locator('.selected-option')).toHaveCount(1);
    await expect(editor.getNavList().locator('a.locale')).toHaveCount(1);
  });

  test('should display "Save" button when user settings say so', async ({ page }) => {
    // given, when
    await editor.navigateTo();
    await editor.getNavList().locator('a.locale').first().click();

    // then
    await expect(page.locator('.save-button')).toHaveText('Save');
  });

  test('should display "Save and next" button when user settings say so', async ({ page }) => {
    // given
    await remockApi(page, '/api/me*', 'me-save-behavior-saveandnext');

    // when
    await editor.navigateTo();
    await editor.getNavList().locator('a.locale').first().click();

    // then
    await expect(page.locator('.save-button')).toHaveText('Save and next');
  });

  test('should call updateSettings on "Save and next"', async ({ page }) => {
    // given
    await mockApi(page, '/api/message', 'johndoe/p1/message', { method: 'PUT' });
    await mockApi(page, '/api/user/*/settings', 'me-save-behavior-saveandnext', { method: 'PATCH' });

    // when
    await editor.navigateTo();
    await editor.getNavList().locator('a.locale').first().click();
    const updateSettings = waitForApi(page, '/api/user/*/settings', 'PATCH');
    await page.locator('.menu-button').click();
    await page.locator('.save-behavior-saveandnext').click();

    // then
    expect((await updateSettings).postDataJSON()).toEqual({ 'save-behavior': 'saveandnext' });
    await expect(editor.getNavList().locator('a.locale').nth(1)).toHaveClass(/\bactive\b/);
  });
});
