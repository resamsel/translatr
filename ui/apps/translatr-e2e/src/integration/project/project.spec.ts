import { test, expect } from '../../support/test';
import { mockApi } from '../../support/mock-api';
import { ProjectPage } from '../../support/project/project-page.po';

test.describe('Project', () => {
  let projectPage: ProjectPage;

  test.beforeEach(async ({ page }) => {
    projectPage = new ProjectPage(page, 'johndoe', 'p1');

    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
    await mockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
  });

  test('should have page name p1', async () => {
    // given

    // when
    await projectPage.navigateTo();

    // then
    await expect(projectPage.getPageName()).toHaveText('johndoe/p1');
  });

  test('should show locale, key and member counts on the overview', async ({ page }) => {
    // given

    // when
    await projectPage.navigateTo();

    // then
    await expect(page.locator('app-project-info')).toHaveCount(1);
    await expect(page.locator('app-project-info dev-metric.locale.count mat-card-title')).toHaveText('2');
    await expect(page.locator('app-project-info dev-metric.key.count mat-card-title')).toHaveText('2');
    await expect(page.locator('app-project-info dev-metric.member.count mat-card-title')).toHaveText('4');
  });

  test('should render the infographic when its feature flag is present', async ({ page }) => {
    // given
    await mockApi(page, '/api/me?fetch=features', 'me-infographic');

    // when
    await projectPage.navigateTo();

    // then
    await expect(page.locator('dev-project-infographic')).toHaveCount(1);
  });

  test('should not render the infographic without its feature flag', async ({ page }) => {
    // given

    // when
    await projectPage.navigateTo();

    // then
    await expect(page.locator('app-project-info')).toHaveCount(1);
    await expect(page.locator('dev-project-infographic')).toHaveCount(0);
  });
});
