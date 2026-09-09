import { test, expect } from '../../../support/test';
import { mockApi, remockApi } from '../../../support/mock-api';
import { ProjectLocalesPage } from '../../../support/project/project-locales-page.po';

test.describe('Project Locales Add Locale Visibility', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
    await remockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
  });

  test('should not show locale add button for member role Developer', async ({ page }) => {
    // given
    await remockApi(page, '/api/me*', 'janesmith');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1-developer');

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();

    // then
    await expect(locales.getFloatingActionButton()).toHaveCount(0);
  });

  test('should show locale add button for member role Translator', async ({ page }) => {
    // given
    await remockApi(page, '/api/me*', 'sophiaoreilly');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1-translator');

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();

    // then
    await expect(locales.getFloatingActionButton()).toHaveCount(1);
  });

  test('should show locale add button for member role Manager', async ({ page }) => {
    // given
    await remockApi(page, '/api/me*', 'ronnylee');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1-manager');

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();

    // then
    await expect(locales.getFloatingActionButton()).toHaveCount(1);
  });

  test('should show locale add button for user role Admin', async ({ page }) => {
    // given
    await remockApi(page, '/api/me*', 'anneearth');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');

    // when
    const locales = await new ProjectLocalesPage(page, 'johndoe', 'p1').navigateTo();

    // then
    await expect(locales.getFloatingActionButton()).toHaveCount(1);
  });
});
