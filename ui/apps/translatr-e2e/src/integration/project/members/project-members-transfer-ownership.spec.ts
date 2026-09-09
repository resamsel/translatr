import { test, expect } from '../../../support/test';
import { mockApi, waitForApi } from '../../../support/mock-api';
import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

test.describe('Project Members Transfer Ownership', () => {
  let members: ProjectMembersPage;

  test.beforeEach(async ({ page }) => {
    members = new ProjectMembersPage(page, 'johndoe', 'p1');

    await mockApi(page, '/api/me?fetch=features', 'me');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members-two-owners');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
    await mockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
  });

  test('should show the transfer-ownership button on an owner row when two owners exist', async () => {
    // given

    // when
    await members.navigateTo();

    // then
    await expect(members.getMemberRow('John Doe').locator('button.edit mat-icon')).toContainText(
      'swap_horiz',
    );
  });

  test('should open the owner edit dialog on clicking the transfer button', async () => {
    // given

    // when
    await members.navigateTo();
    await members.getMemberRow('John Doe').locator('button.edit').click();

    // then
    await expect(members.getDialog()).toHaveCount(1);
    await expect(members.getDialogTitle()).toHaveAttribute(
      'transloco',
      'project.transferOwnership.title',
    );
  });

  test('should transfer ownership and return to the members page on save', async ({ page }) => {
    // given
    await mockApi(page, '/api/users*', 'johndoe/p1/users-mika');
    await mockApi(page, '/api/project', 'johndoe/p1', { method: 'PUT' });

    // when
    await members.navigateTo();
    await members.getMemberRow('John Doe').locator('button.edit').click();
    await members.getDialog().locator('input').first().fill('ronnylee');
    await page.getByRole('option', { name: 'ronnylee' }).click();
    const updateProject = waitForApi(page, '/api/project', 'PUT');
    await members.getDialogSaveButton().click();

    // then
    expect((await updateProject).postDataJSON()).toMatchObject({
      ownerId: '5e15a05d-c583-45a0-84fa-1e770b2a4534',
    });
    await expect(members.getDialog()).toHaveCount(0);
    await expect(page).toHaveURL(/\/johndoe\/p1\/members/);
  });
});
