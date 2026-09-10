import { test, expect } from '../../support/test';
import { mockApi } from '../../support/mock-api';
import { DashboardPage } from '../../support/dashboard.po';

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/users?limit=1&fetch=count', 'dashboard/users-limit1');
    await mockApi(page, '/api/projects?*ownerUsername=*', 'dashboard/projects-owner-limit4');
    await mockApi(page, '/api/projects?*memberId=*', 'dashboard/projects-memberId-limit4');
    await mockApi(page, '/api/activities*', 'dashboard/activities-userId-limit4');
  });

  test('should have page name Dashboard', async ({ page }) => {
    // given

    // when
    const dashboard = await new DashboardPage(page).navigateTo();

    // then
    await expect(dashboard.getPageName()).toHaveText('Dashboard');
  });

  test('should have metrics values', async ({ page }) => {
    // given

    // when
    const dashboard = await new DashboardPage(page).navigateTo();

    // then
    await expect(dashboard.getProjectCardLinks()).toHaveCount(4);
    await expect(dashboard.getProjectEmptyView()).toHaveCount(0);
    await expect(dashboard.getMetric('my.project').locator('mat-card-title')).toHaveText('11');
    await expect(dashboard.getMetric('my.activity').locator('mat-card-title')).toHaveText('1.6k');
    await expect(dashboard.getMetric('all.project').locator('mat-card-title')).toHaveText('13');
    await expect(dashboard.getMetric('user').locator('mat-card-title')).toHaveText('10.4k');
  });
});
