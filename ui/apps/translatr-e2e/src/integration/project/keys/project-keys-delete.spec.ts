import { test, expect } from '../../../support/test';
import { mockApi, remockApi } from '../../../support/mock-api';
import { ProjectKeysPage } from '../../../support/project/project-keys-page.po';

test.describe('Project Keys Delete Key', () => {
  let keys: ProjectKeysPage;

  test.beforeEach(async ({ page }) => {
    keys = new ProjectKeysPage(page, 'johndoe', 'p1');

    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
    await remockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
  });

  test('should show key delete button', async () => {
    // given

    // when
    await keys.navigateTo();

    // then
    await expect(keys.getKeyList().locator('confirm-button.delete').first()).toBeVisible();
  });

  test('should show key delete menu on clicking delete button', async ({ page }) => {
    // given

    // when
    await keys.navigateTo();
    await keys.getKeyList().locator('confirm-button.delete').first().click();

    // then
    await expect(page.locator('.mat-mdc-menu-panel button.confirm')).toHaveText('Remove');
  });

  test('should delete key clicking delete button', async ({ page }) => {
    // given
    await mockApi(page, '/api/key/*', 'johndoe/p1/keys/k1', { method: 'DELETE' });

    // when
    await keys.navigateTo();
    await keys.getKeyList().locator('confirm-button.delete').first().click();
    await page.locator('.mat-mdc-menu-panel button.confirm').click();

    // then
    await expect(keys.getKeyList().locator('a[mat-list-item]')).toHaveCount(1);
  });
});
