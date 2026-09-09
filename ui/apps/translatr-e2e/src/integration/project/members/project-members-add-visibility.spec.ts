import { test, expect } from '../../../support/test';
import { mockApi } from '../../../support/mock-api';
import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

test.describe('Project Members Add Member Visibility', () => {
  let members: ProjectMembersPage;

  test.beforeEach(async ({ page }) => {
    members = new ProjectMembersPage(page, 'johndoe', 'p1');

    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
    await mockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
  });

  test('should not show the add member button for member role Developer', async ({ page }) => {
    // given
    await mockApi(page, '/api/me?fetch=features', 'janesmith');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1-developer');

    // when
    await members.navigateTo();

    // then
    await expect(members.getFloatingActionButton()).toHaveCount(0);
  });

  test('should not show the add member button for member role Translator', async ({ page }) => {
    // given
    await mockApi(page, '/api/me?fetch=features', 'sophiaoreilly');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1-translator');

    // when
    await members.navigateTo();

    // then
    await expect(members.getFloatingActionButton()).toHaveCount(0);
  });

  test('should show the add member button for member role Manager', async ({ page }) => {
    // given
    await mockApi(page, '/api/me?fetch=features', 'ronnylee');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1-manager');

    // when
    await members.navigateTo();

    // then
    await expect(members.getFloatingActionButton()).toHaveCount(1);
  });

  test('should show the add member button for user role Admin', async ({ page }) => {
    // given
    await mockApi(page, '/api/me?fetch=features', 'anneearth');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');

    // when
    await members.navigateTo();

    // then
    await expect(members.getFloatingActionButton()).toHaveCount(1);
  });
});
