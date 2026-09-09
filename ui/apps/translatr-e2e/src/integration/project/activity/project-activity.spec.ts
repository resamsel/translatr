import { test, expect } from '../../../support/test';
import { mockApi } from '../../../support/mock-api';
import { ProjectActivityPage } from '../../../support/project/project-activity-page.po';

test.describe('Project Activity', () => {
  let activityPage: ProjectActivityPage;

  test.beforeEach(async ({ page }) => {
    activityPage = new ProjectActivityPage(page, 'johndoe', 'p1');

    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
    await mockApi(page, '/api/activities*', 'johndoe/p1/activities');
    await mockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
  });

  test('should render the project activity list and graph', async ({ page }) => {
    // given

    // when
    await activityPage.navigateTo();

    // then
    await expect(page.locator('app-activity-list')).toHaveCount(1);
    await expect(activityPage.getActivityGraph()).toHaveCount(1);
    await expect(activityPage.getActivityRows()).toHaveCount(1);
  });
});
