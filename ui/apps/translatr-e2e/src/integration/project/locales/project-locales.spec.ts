import { test, expect } from '../../../support/test';
import { mockApi } from '../../../support/mock-api';
import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

test.describe('Project Locales', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
  });

  test('should have page name Project Locales', async ({ page }) => {
    // given

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();

    // then
    await expect(locales.getPageName()).toHaveText('johndoe/p1');
  });
});
