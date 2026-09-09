import { test, expect } from '../../../support/test';
import { mockApi, remockApi } from '../../../support/mock-api';
import { ProjectKeysPage } from '../../../support/project/project-keys-page.po';

test.describe('Project Keys Edit Key Visibility', () => {
  let keys: ProjectKeysPage;

  test.beforeEach(async ({ page }) => {
    keys = new ProjectKeysPage(page, 'johndoe', 'p1');

    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
    await remockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
  });

  test('should show key delete button for member role Developer', async ({ page }) => {
    // given
    await remockApi(page, '/api/me*', 'janesmith');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1-developer');

    // when
    await keys.navigateTo();

    // then
    await expect(keys.getKeyList().locator('button.edit')).toHaveCount(2);
  });

  test('should not show key delete button for member role Translator', async ({ page }) => {
    // given
    await remockApi(page, '/api/me*', 'sophiaoreilly');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1-translator');

    // when
    await keys.navigateTo();

    // then
    await expect(keys.getKeyList().locator('button.edit')).toHaveCount(0);
  });

  test('should show key delete button for member role Manager', async ({ page }) => {
    // given
    await remockApi(page, '/api/me*', 'ronnylee');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1-manager');

    // when
    await keys.navigateTo();

    // then
    await expect(keys.getKeyList().locator('button.edit')).toHaveCount(2);
  });

  test('should show key delete button for user role Admin', async ({ page }) => {
    // given
    await remockApi(page, '/api/me*', 'anneearth');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');

    // when
    await keys.navigateTo();

    // then
    await expect(keys.getKeyList().locator('button.edit')).toHaveCount(2);
  });
});
