import { test, expect } from '../../../support/test';
import { mockApi, mockApiWith, remockApi, waitForApi } from '../../../support/mock-api';
import { KeyEditorPage } from '../../../support/project/key-editor-page.po';

test.describe('Project Key Editor Save', () => {
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

  test('should persist the translation on Save', async ({ page }) => {
    // given
    await mockApiWith(
      page,
      '/api/message',
      async (route) => {
        const body = route.request().postDataJSON();
        await route.fulfill({ status: 200, json: { ...body, id: 'm-new' } });
      },
      { method: 'PUT' },
    );

    // when
    await editor.navigateTo();
    await editor.getNavList().locator('a.locale').first().click();
    // PORT-NOTE: cy `.type('Hallo Welt', { force: true })` into the CodeMirror
    // textarea -> Playwright `fill(..., { force: true })` (guide's `.type` -> `.fill`).
    await editor.getEditor().locator('.CodeMirror textarea').fill('Hallo Welt', { force: true });

    const saveMessage = waitForApi(page, '/api/message', 'PUT');
    await page.locator('.save-button').click();

    // then
    const request = await saveMessage;
    expect(request.postDataJSON().value).toContain('Hallo Welt');
    expect(request.postDataJSON().id).toBe('6c1f75a8-903b-4251-b704-af0efd836c65');
    await expect(page.locator('.save-button')).toHaveText('Save');
    await expect(editor.getNavList().locator('a.locale.active .translation')).toContainText('Hallo Welt');
  });

  test('should advance to the next locale on "Save and next"', async ({ page }) => {
    // given
    await remockApi(page, '/api/me*', 'me-save-behavior-saveandnext');
    await mockApiWith(
      page,
      '/api/message',
      async (route) => {
        const body = route.request().postDataJSON();
        await route.fulfill({ status: 200, json: { ...body, id: 'm-new' } });
      },
      { method: 'PUT' },
    );

    // when
    await editor.navigateTo();
    const firstLocale = editor.getNavList().locator('a.locale').first();
    await firstLocale.click();
    await expect(firstLocale).toHaveClass(/\bactive\b/);
    // PORT-NOTE: cy `.type('Hallo Welt', { force: true })` -> `fill(..., { force: true })`.
    await editor.getEditor().locator('.CodeMirror textarea').fill('Hallo Welt', { force: true });
    await expect(page.locator('.save-button')).toHaveText('Save and next');
    const saveMessage = waitForApi(page, '/api/message', 'PUT');
    await page.locator('.save-button').click();

    // then
    await saveMessage;
    await expect(editor.getNavList().locator('a.locale').nth(1)).toHaveClass(/\bactive\b/);
    await expect(editor.getNavList().locator('a.locale').first()).not.toHaveClass(/\bactive\b/);
  });
});
