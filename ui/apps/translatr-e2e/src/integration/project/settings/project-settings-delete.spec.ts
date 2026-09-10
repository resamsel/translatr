import { test, expect } from '../../../support/test';
import { mockApi } from '../../../support/mock-api';
import { ProjectSettingsPage } from '../../../support/project/project-settings-page.po';

test.describe('Project Settings Delete', () => {
  let settingsPage: ProjectSettingsPage;

  test.beforeEach(async ({ page }) => {
    settingsPage = new ProjectSettingsPage(page, 'johndoe', 'p1');

    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
    await mockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
  });

  test('should show the delete project button', async () => {
    // given

    // when
    await settingsPage.navigateTo();

    // then
    await expect(settingsPage.getDeleteProjectButton()).toBeVisible();
  });

  test('should show the delete project dialog when on delete project', async () => {
    // given

    // when
    await settingsPage.navigateTo();
    await settingsPage.getDeleteProjectButton().click();

    // then
    await expect(settingsPage.getDeleteProjectDialog()).toHaveCount(1);
  });

  test('should hide the delete project dialog on cancel', async () => {
    // given

    // when
    await settingsPage.navigateTo();
    await settingsPage.getDeleteProjectButton().click();
    await settingsPage.getCancelProjectDeleteDialogButton().click();

    // then
    await expect(settingsPage.getDeleteProjectDialog()).toHaveCount(0);
  });

  test('should disable the delete button when project name empty', async () => {
    // given

    // when
    await settingsPage.navigateTo();
    await settingsPage.getDeleteProjectButton().click();

    // then
    await expect(settingsPage.getDeleteProjectDialog().locator('button.delete')).toBeDisabled();
  });

  test('should disable the delete button when project name incorrect', async () => {
    // given

    // when
    await settingsPage.navigateTo();
    await settingsPage.getDeleteProjectButton().click();
    await settingsPage.getDeleteProjectDialog().locator('input').fill('INCORRECT');

    // then
    await expect(settingsPage.getDeleteProjectDialog().locator('button.delete')).toBeDisabled();
  });

  test('should hide the delete project dialog on successful delete', async ({ page }) => {
    // given
    await mockApi(page, '/api/project/*', 'johndoe/p1', { method: 'DELETE' });

    // dashboard
    await mockApi(page, '/api/users?limit=1&fetch=count', 'dashboard/users-limit1');
    await mockApi(page, '/api/projects?*ownerUsername=*', 'dashboard/projects-owner-limit4');
    await mockApi(page, '/api/projects?*memberId=*', 'dashboard/projects-memberId-limit4');
    await mockApi(page, '/api/activities*', 'dashboard/activities-userId-limit4');

    // when
    await settingsPage.navigateTo();
    await settingsPage.getDeleteProjectButton().click();
    await settingsPage.getDeleteProjectDialog().locator('input').fill('p1');
    await settingsPage.getDeleteProjectDialog().locator('button.delete').click();

    // then
    await expect(settingsPage.getDeleteProjectDialog()).toHaveCount(0);

    await expect(page).toHaveURL(/\/dashboard/);
  });

  // describe('Delete Project as Manager', () => {
  //   beforeEach(() => {
  //     cy.intercept('/api/me?fetch=features', { fixture: 'me' });
  //   });
  //
  //   it('should have', () => {
  //     page.navigateTo();
  //   });
  // });
});
