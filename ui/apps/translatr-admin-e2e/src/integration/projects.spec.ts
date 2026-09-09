import { test, expect } from '../support/test';
import { mockApi, waitForApi } from '../support/mock-api';
import { ProjectsPage } from '../support/projects-page.po';

test.describe('Admin Projects', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/project*', 'projects');
  });

  test('should list the projects returned by the API', async ({ page }) => {
    const projects = await new ProjectsPage(page).navigateTo();

    await expect(projects.getPageName()).toHaveText('Projects');
    await expect(projects.getRows()).toHaveCount(3);
    await expect(projects.getRows().first()).toContainText('alpha');
  });

  test('should push the search term into the projects request', async ({ page }) => {
    await mockApi(page, '/api/project*search=*', 'projects');
    const projects = await new ProjectsPage(page).navigateTo();

    const search = waitForApi(page, '/api/project*search=*');
    await projects.getSearchField().fill('beta');
    await page.locator('mat-option').first().click();

    expect((await search).url()).toContain('search=beta');
  });
});
