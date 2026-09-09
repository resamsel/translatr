import { test, expect } from '../support/test';
import { mockApi } from '../support/mock-api';
import { DashboardPage } from '../support/dashboard-page.po';

test.describe('Admin Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/statistics', 'statistics');
    await mockApi(page, '/api/user*', 'users');
    await mockApi(page, '/api/project*', 'projects');
    await mockApi(page, '/api/accesstokens*', 'access-tokens');
    await mockApi(page, '/api/activities*', 'activities');
    await mockApi(page, '/api/featureflags*', {
      list: [],
      offset: 0,
      limit: 20,
      total: 0,
      hasPrev: false,
      hasNext: false,
    });
  });

  test('should render the dashboard with the page title', async ({ page }) => {
    const dashboard = await new DashboardPage(page).navigateTo();

    await expect(dashboard.getPageName()).toHaveText('Dashboard');
  });

  test('should render the four metric cards with totals from the API', async ({ page }) => {
    const dashboard = await new DashboardPage(page).navigateTo();

    await expect(dashboard.getMetric('user').first().locator('mat-card-title')).toHaveText('3');
    await expect(dashboard.getMetric('project').first().locator('mat-card-title')).toHaveText('3');
    await expect(
      dashboard.getMetric('access-token').first().locator('mat-card-title'),
    ).toHaveText('2');
    await expect(dashboard.getMetric('activity').first().locator('mat-card-title')).toHaveText('3');
  });

  test('should show the latest user name in the secondary metric', async ({ page }) => {
    const dashboard = await new DashboardPage(page).navigateTo();

    await expect(dashboard.getMetric('user').nth(1).locator('mat-card-title')).toHaveText(
      'Jane Smith',
    );
  });
});
