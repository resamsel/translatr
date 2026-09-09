import { test, expect } from '../../../support/test';
import { mockApi } from '../../../support/mock-api';
import { UserProjectsPage } from '../../../support/user/user-projects-page.po';

test.describe('User Projects More', () => {
  let projectsPage: UserProjectsPage;

  test.beforeEach(async ({ page }) => {
    projectsPage = new UserProjectsPage(page, 'janesmith');

    await mockApi(page, '/api/janesmith', 'janesmith');
    await mockApi(page, '/api/projects*', 'janesmith/projects');
    await mockApi(page, '/api/activities*', 'johndoe/activities');
  });

  test('should show more button', async ({ page }) => {
    // given

    // when
    await projectsPage.navigateTo();

    // then
    await expect(page.locator('a.more')).toHaveCount(1);
  });
});
