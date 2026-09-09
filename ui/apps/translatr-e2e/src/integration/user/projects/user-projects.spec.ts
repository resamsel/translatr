import { test, expect } from '../../../support/test';
import { mockApi } from '../../../support/mock-api';
import { UserProjectsPage } from '../../../support/user/user-projects-page.po';

test.describe('User Projects', () => {
  let projectsPage: UserProjectsPage;

  test.beforeEach(async ({ page }) => {
    projectsPage = new UserProjectsPage(page, 'johndoe');

    await mockApi(page, '/api/johndoe', 'johndoe');
    await mockApi(page, '/api/projects*', 'johndoe/projects');
    await mockApi(page, '/api/activities*', 'johndoe/activities');
  });

  test('should show user projects', async () => {
    // given

    // when
    await projectsPage.navigateTo();

    // then
    await expect(projectsPage.getPageName()).toHaveText('John Doe');
  });

  test('should not show more button', async ({ page }) => {
    // given

    // when
    await projectsPage.navigateTo();

    // then
    await expect(page.locator('a.more')).toHaveCount(0);
  });
});
