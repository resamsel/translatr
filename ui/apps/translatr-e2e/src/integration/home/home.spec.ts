import { test, expect } from '../../support/test';
import { mockApi } from '../../support/mock-api';
import { HomePage } from '../../support/app.po';

test.describe('Home', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/me?fetch=features', 'me');
    await mockApi(page, '/api/statistics', 'statistics');
    await mockApi(page, '/api/activities/aggregated', 'activities-aggregated');
  });

  test('should have page name Home', async ({ page }) => {
    // when
    const home = await new HomePage(page).navigateTo();

    // then
    await expect(home.getPageName()).toHaveText('Translatr');
  });

  test('should have project metric value', async ({ page }) => {
    // when
    const home = await new HomePage(page).navigateTo();

    // then
    await expect(home.getProjectMetricValue()).toHaveText('15');
  });

  test('should have user metric value', async ({ page }) => {
    // when
    const home = await new HomePage(page).navigateTo();

    // then
    await expect(home.getUserMetricValue()).toHaveText('25');
  });

  test('should have activity metric value', async ({ page }) => {
    // when
    const home = await new HomePage(page).navigateTo();

    // then
    await expect(home.getActivityMetricValue()).toHaveText('100');
  });
});
