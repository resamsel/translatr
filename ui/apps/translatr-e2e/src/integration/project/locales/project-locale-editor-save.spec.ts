import { test, expect } from '../../../support/test';
import { mockApi, mockApiWith, remockApi, waitForApi } from '../../../support/mock-api';
import { LocaleEditorPage } from '../../../support/project/locale-editor-page.po';

test.describe('Project Locale Editor Save', () => {
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

  test('should persist the translation on Save', async ({ page }) => {
    // given
    await mockApiWith(
      page,
      '/api/message',
      async (route) => {
        await route.fulfill({ json: { ...route.request().postDataJSON(), id: 'm-new' } });
      },
      { method: 'PUT' },
    );

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();
    await editor.getNavList().locator('a.key').first().click();
    // PORT-NOTE: CodeMirror's backing textarea is hidden; `fill` with `force: true`
    // mirrors the old `.type('Bonjour', { force: true })` bypass of actionability.
    await editor.getEditor().locator('.CodeMirror textarea').fill('Bonjour', { force: true });

    const saveMessage = waitForApi(page, '/api/message', 'PUT');
    await page.locator('.save-button').click();

    // then
    const body = (await saveMessage).postDataJSON();
    expect(body.value).toContain('Bonjour');
    expect(body.id).toBe('d8a87a83-ad7f-42b5-ade4-94a0e5cf54b3');
    await expect(page.locator('.save-button')).toHaveText('Save');
    await expect(editor.getNavList().locator('a.key.active .translation')).toContainText('Bonjour');
  });

  test('should advance to the next key on "Save and next"', async ({ page }) => {
    // given
    await remockApi(page, '/api/me*', 'me-save-behavior-saveandnext');
    await mockApiWith(
      page,
      '/api/message',
      async (route) => {
        await route.fulfill({ json: { ...route.request().postDataJSON(), id: 'm-new' } });
      },
      { method: 'PUT' },
    );

    // when
    const editor = await new LocaleEditorPage(page, 'johndoe', 'p1', 'default').navigateTo();
    const firstKey = editor.getNavList().locator('a.key').first();
    await firstKey.click();
    await expect(firstKey).toHaveClass(/\bactive\b/);
    // PORT-NOTE: see sibling test - hidden CodeMirror textarea, forced fill.
    await editor.getEditor().locator('.CodeMirror textarea').fill('Bonjour', { force: true });
    await expect(page.locator('.save-button')).toHaveText('Save and next');

    const saveMessage = waitForApi(page, '/api/message', 'PUT');
    await page.locator('.save-button').click();

    // then
    await saveMessage;
    await expect(editor.getNavList().locator('a.key').nth(1)).toHaveClass(/\bactive\b/);
    await expect(editor.getNavList().locator('a.key').first()).not.toHaveClass(/\bactive\b/);
  });
});
