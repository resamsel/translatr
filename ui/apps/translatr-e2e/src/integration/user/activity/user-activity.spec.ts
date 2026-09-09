import { test, expect } from '../../../support/test';
import { mockApi, remockApi, waitForApi } from '../../../support/mock-api';
import { UserActivityPage } from '../../../support/user/user-activity-page.po';

test.describe('User Activity', () => {
  let activityPage: UserActivityPage;

  test.beforeEach(async ({ page }) => {
    activityPage = new UserActivityPage(page, 'johndoe');

    await mockApi(page, '/api/johndoe', 'johndoe');
    await mockApi(page, '/api/user/*/activity*', 'johndoe/activities');
    await mockApi(page, '/api/activities*', 'johndoe/activities');
    await remockApi(page, '/api/activities/aggregated*', 'activities-aggregated');
  });

  test('should render the activity list', async ({ page }) => {
    // given

    // when
    await activityPage.navigateTo();

    // then
    await expect(page.locator('app-activity-list')).toHaveCount(1);
    await expect(activityPage.getActivityRows()).toHaveCount(3);
  });

  test('should navigate to the user on an activity link click', async ({ page }) => {
    // given
    await mockApi(page, '/api/projects*', 'johndoe/projects');

    // when
    await activityPage.navigateTo();
    await activityPage.getUserLink().first().click();

    // then
    await expect(page).toHaveURL(/\/johndoe$/);
  });

  test('should load more activities', async ({ page }) => {
    // given
    await mockApi(page, '/api/activities*limit=8*', 'johndoe/activities-page2');

    // when
    await activityPage.navigateTo();
    await expect(activityPage.getActivityRows()).toHaveCount(3);
    const more = waitForApi(page, '/api/activities*limit=8*');
    await activityPage.getLoadMoreButton().click();

    // then
    expect((await more).url()).toContain('limit=8');
    await expect(activityPage.getActivityRows()).toHaveCount(2);
  });
});
