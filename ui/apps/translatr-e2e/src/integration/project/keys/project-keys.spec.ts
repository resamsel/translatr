import { test, expect } from '../../../support/test';
import { mockApi, remockApi } from '../../../support/mock-api';
import { ProjectKeysPage } from '../../../support/project/project-keys-page.po';

test.describe('Project Keys', () => {
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

  test('should have page name Project Keys', async () => {
    // given

    // when
    await keys.navigateTo();

    // then
    await expect(keys.getPageName()).toHaveText('johndoe/p1');
  });
});
