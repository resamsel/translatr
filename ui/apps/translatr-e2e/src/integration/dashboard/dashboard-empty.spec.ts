import { test, expect } from '../../support/test';
import { mockApi } from '../../support/mock-api';
import { DashboardPage } from '../../support/dashboard.po';

test.describe('Dashboard Empty', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/users?limit=1&fetch=count', 'dashboard/empty/users-limit1');
    await mockApi(page, '/api/projects?*ownerUsername=*', 'dashboard/empty/projects-owner-limit4');
    await mockApi(page, '/api/projects?*memberId=*', 'dashboard/empty/projects-memberId-limit4');
    await mockApi(page, '/api/activities*', 'dashboard/empty/activities-userId-limit4');
  });

  test('should show teasers for empty contents', async ({ page }) => {
    // given

    // when
    const dashboard = await new DashboardPage(page).navigateTo();

    // then
    await expect(dashboard.getProjectCardLinks()).toHaveCount(0);
    await expect(dashboard.getProjectEmptyView()).toHaveCount(1);
  });

  test('should show dialog when using teaser button', async ({ page }) => {
    // given

    // when
    const dashboard = await new DashboardPage(page).navigateTo();

    // then
    await dashboard.getProjectEmptyView().locator('button').click();
    await expect(dashboard.getProjectCreationDialog()).toBeVisible();
  });

  test('should show dialog when using floating action button', async ({ page }) => {
    // given

    // when
    const dashboard = await new DashboardPage(page).navigateTo();

    // then
    await dashboard.getFloatingActionButton().click();
    await expect(dashboard.getProjectCreationDialog()).toBeVisible();
  });
});
