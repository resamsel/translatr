import { test, expect } from '../../../support/test';
import { mockApi, remockApi } from '../../../support/mock-api';
import { ProjectKeysPage } from '../../../support/project/project-keys-page.po';

test.describe('Project Keys Edit Key', () => {
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

  test('should show key edit button', async () => {
    // given

    // when
    await keys.navigateTo();

    // then
    await expect(keys.getKeyList().locator('button.edit').first()).toBeVisible();
  });

  test('should show key edit dialog on clicking edit button', async () => {
    // given

    // when
    await keys.navigateTo();
    await keys.getKeyList().locator('button.edit').first().click();

    // then
    await expect(keys.getDialog()).toBeVisible();
    await expect(keys.getDialog().locator('[mat-dialog-title]')).toHaveText('Edit Key');
    await expect(keys.getDialog().locator('mat-form-field.name input')).toHaveValue('k1');
  });

  test('should hide key edit dialog on clicking cancel button', async () => {
    // given

    // when
    await keys.navigateTo();
    await keys.getKeyList().locator('button.edit').first().click();
    await keys.getDialog().locator('button.cancel').click();

    // then
    await expect(keys.getDialog()).toHaveCount(0);
  });
});
