import { test, expect } from '../../../support/test';
import { mockApi, remockApi, waitForApi } from '../../../support/mock-api';
import { LocaleEditorPage } from '../../../support/project/locale-editor-page.po';

test.describe('Project Locale Editor', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/johndoe/p1/locales/default', 'johndoe/p1/locales/default');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/keys?*missing=true*', 'johndoe/p1/keys-missing');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages-locale-default');
    await mockApi(page, '/api/project/*/messages?*keyName=k1', 'johndoe/p1/messages-key-k1');
    await mockApi(page, '/api/project/*/messages?*keyIds=*', 'johndoe/p1/messages-locale-default');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
  });

  test('should have page title Language Editor', async ({ page }) => {
    // given

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();

    // then
    await expect(editor.getPageTitle()).toHaveText('Language Editor');
  });

  test('should have locale default selected in sidebar', async ({ page }) => {
    // given

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();

    // then
    await expect(editor.getSelectedLocaleField()).toHaveText('default');
  });

  test('should have two keys in sidebar', async ({ page }) => {
    // given

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();

    // then
    await expect(editor.getNavList().locator('a.key')).toHaveCount(2);
  });

  test('should have no key selected in sidebar', async ({ page }) => {
    // given

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();

    // then
    await expect(editor.getNavList().locator('a.active')).toHaveCount(0);
  });

  test('should have key selected when activated in sidebar', async ({ page }) => {
    // given

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();

    // then
    await expect(editor.getNavList().locator('a.key:first-of-type')).not.toHaveClass(/\bactive\b/);
    const firstKey = editor.getNavList().locator('a.key').first();
    await firstKey.click();
    await expect(firstKey).toHaveClass(/\bactive\b/);
  });

  test('should show editor when key activated in sidebar', async ({ page }) => {
    // given

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();

    // then
    await editor.getNavList().locator('a.key').first().click();

    await expect(editor.getEditor()).toBeVisible();
  });

  test('should show translation when key activated in sidebar', async ({ page }) => {
    // given

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();
    await editor.getNavList().locator('a.key').first().click();

    // then
    await expect(editor.getEditorContents()).toHaveText('Key One');
  });

  test('should show meta when key activated in sidebar', async ({ page }) => {
    // given

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();

    // then
    await editor.getNavList().locator('a.key').first().click();

    await expect(editor.getMeta()).toBeVisible();
  });

  test('should show preview when key activated in sidebar', async ({ page }) => {
    // given
    await remockApi(page, '/api/project/*/messages?*keyIds=*', 'johndoe/p1/messages-locale-default');

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();
    await editor.getNavList().locator('a.key').first().click();

    // then
    await expect(editor.getPreviewContents()).toHaveText('Key One');
  });

  test('should show existing translations when translations tab selected', async ({ page }) => {
    // given

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();

    // then
    await editor.getNavList().locator('a.key').first().click();

    await editor.getTranslationsTab().click();
    await expect(editor.getTranslationsBody().locator('mat-card')).toHaveCount(2);
  });

  test('should use translation when use translation is clicked', async ({ page }) => {
    // given

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();
    await editor.getNavList().locator('a.key').first().click();

    await editor.getTranslationsTab().click();
    await page.locator('.meta [role="tabpanel"] mat-card button.use-value').first().click();

    // then
    await expect(editor.getEditorContents()).toHaveText('Schlüssel 1');
  });

  test('should only show keys with missing translations when filtered by those', async ({ page }) => {
    // given
    await remockApi(page, '/api/project/*/messages?*keyIds=*', 'johndoe/p1/messages-missing');

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();

    await editor.getFilterField().focus();
    await page.locator('.autocomplete-option').first().click();

    // then
    await expect(page.locator('.selected-option')).toHaveCount(1);
    await expect(editor.getNavList().locator('a.key')).toHaveCount(1);
  });

  test('should display "Save" button when user settings say so', async ({ page }) => {
    // given, when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();
    await editor.getNavList().locator('a.key').first().click();

    // then
    await expect(page.locator('.save-button')).toHaveText('Save');
  });

  test('should display "Save and next" button when user settings say so', async ({ page }) => {
    // given
    await remockApi(page, '/api/me*', 'me-save-behavior-saveandnext');

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();
    await editor.getNavList().locator('a.key').first().click();

    // then
    await expect(page.locator('.save-button')).toHaveText('Save and next');
  });

  test('should call updateSettings on "Save and next"', async ({ page }) => {
    // given
    await mockApi(page, '/api/message', 'johndoe/p1/message', { method: 'PUT' });
    await mockApi(page, '/api/user/*/settings', 'me-save-behavior-saveandnext', { method: 'PATCH' });

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();
    await editor.getNavList().locator('a.key').first().click();
    await page.locator('.menu-button').click();

    const updateSettings = waitForApi(page, '/api/user/*/settings', 'PATCH');
    await page.locator('.save-behavior-saveandnext').click();

    // then
    expect((await updateSettings).postDataJSON()).toEqual({ 'save-behavior': 'saveandnext' });
    await expect(editor.getNavList().locator('a.key').nth(1)).toHaveClass(/\bactive\b/);
  });
});
