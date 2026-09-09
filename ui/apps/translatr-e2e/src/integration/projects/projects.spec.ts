import { test, expect } from '../../support/test';
import { mockApi, waitForApi } from '../../support/mock-api';
import { ProjectsPage } from '../../support/projects-page.po';

test.describe('Projects', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, '/api/project*', 'projects');
  });

  test('should list projects', async ({ page }) => {
    // given

    // when
    const projects = await new ProjectsPage(page).navigateTo();

    // then
    await expect(projects.getCards()).toHaveCount(3);
  });

  test('should filter on search input', async ({ page }) => {
    // given
    await mockApi(page, '/api/project*search=*', 'projects-search');

    // when
    const projects = await new ProjectsPage(page).navigateTo();
    const s = waitForApi(page, '/api/project*search=*', 'GET');
    await projects.getSearchField().pressSequentially('p1');
    await page.locator('mat-option').first().click();

    // then
    expect((await s).url()).toContain('search=p1');
    await expect(projects.getCards()).toHaveCount(1);
  });

  test('should create a project from the floating action button and redirect', async ({ page }) => {
    // given
    await mockApi(page, '/api/project', 'project-created', { method: 'POST' });
    await mockApi(page, '/api/johndoe/newproj*', 'project-created');
    await mockApi(page, '/api/project/*/locales*', { list: [], offset: 0, limit: 50, total: 0 });
    await mockApi(page, '/api/project/*/keys*', { list: [], offset: 0, limit: 50, total: 0 });
    await mockApi(page, '/api/project/*/members*', { list: [], offset: 0, limit: 50, total: 0 });
    await mockApi(page, '/api/activities*', { list: [], offset: 0, limit: 20, total: 0 });
    await mockApi(page, '/api/activities/aggregated*', 'activities-aggregated');

    // when
    const projects = await new ProjectsPage(page).navigateTo();
    await projects.getFloatingActionButton().click();
    await projects.getDialog().locator('input').first().fill('newproj');
    const saveButton = projects.getDialog().locator('button[transloco="button.save"]');
    await expect(saveButton).toBeEnabled();
    const create = waitForApi(page, '/api/project', 'POST');
    await saveButton.click();

    // then
    expect((await create).postDataJSON()).toMatchObject({ name: 'newproj' });
    await expect(page).toHaveURL(/\/johndoe\/newproj/);
  });
});
