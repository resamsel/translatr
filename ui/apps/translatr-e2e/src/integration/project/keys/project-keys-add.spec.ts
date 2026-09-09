import { test, expect } from '../../../support/test';
import { mockApi, remockApi, waitForApi } from '../../../support/mock-api';
import { ProjectKeysPage } from '../../../support/project/project-keys-page.po';

test.describe('Project Keys Add Key', () => {
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

  test('should show key add button', async () => {
    // given

    // when
    await keys.navigateTo();

    // then
    await expect(keys.getFloatingActionButton()).toBeVisible();
  });

  test('should show key add dialog on clicking add button', async () => {
    // given

    // when
    await keys.navigateTo();
    await keys.getFloatingActionButton().click();

    // then
    await expect(keys.getDialog()).toBeVisible();
    await expect(keys.getDialog().locator('mat-form-field.name input')).toHaveValue('');
    await expect(keys.getDialog().locator('[mat-dialog-title]')).toHaveText('Add Key');
  });

  test('should hide key add dialog on clicking cancel button', async () => {
    // given

    // when
    await keys.navigateTo();
    await keys.getFloatingActionButton().click();
    await keys.getDialog().locator('button.cancel').click();

    // then
    await expect(keys.getDialog()).toHaveCount(0);
  });

  test('should add the key and open the new key on save', async ({ page }) => {
    // given
    await mockApi(page, '/api/key', 'johndoe/p1/key-created', { method: 'POST' });
    await mockApi(page, '/api/johndoe/p1/keys/home.title', 'johndoe/p1/key-created');

    // when
    await keys.navigateTo();
    await keys.getFloatingActionButton().click();
    await keys.getDialog().locator('mat-form-field.name input').fill('home.title');

    const createKey = waitForApi(page, '/api/key', 'POST');
    await keys.getDialog().locator('button[transloco="button.save"], button.save').click();
    await createKey;

    // the base `keys*` list is swapped for `keys-added` once the POST has landed
    await remockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys-added');

    // then
    expect((await createKey).postDataJSON()).toMatchObject({ name: 'home.title' });
    await expect(keys.getDialog()).toHaveCount(0);
    await expect(page).toHaveURL(/\/johndoe\/p1\/keys\/home\.title$/);
  });
});
