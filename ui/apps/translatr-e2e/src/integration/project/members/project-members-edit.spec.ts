import { test, expect } from '../../../support/test';
import { mockApi, waitForApi } from '../../../support/mock-api';
import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

test.describe('Project Members Edit Member', () => {
  let members: ProjectMembersPage;

  test.beforeEach(async ({ page }) => {
    members = new ProjectMembersPage(page, 'johndoe', 'p1');

    await mockApi(page, '/api/me?fetch=features', 'me');
    await mockApi(page, '/api/johndoe/p1*', 'johndoe/p1');
    await mockApi(page, '/api/project/*/locales*', 'johndoe/p1/locales');
    await mockApi(page, '/api/project/*/keys*', 'johndoe/p1/keys');
    await mockApi(page, '/api/project/*/messages*', 'johndoe/p1/messages');
    await mockApi(page, '/api/project/*/members*', 'johndoe/p1/members');
    await mockApi(page, '/api/project/*/activities*', 'johndoe/p1/activities');
    await mockApi(page, '/api/activities/aggregated*', 'johndoe/p1/activities-aggregated');
  });

  test('should show the edit button on a non-owner row', async () => {
    // given

    // when
    await members.navigateTo();

    // then
    await expect(members.getEditButton('Jane Smith')).toBeVisible();
  });

  test('should open the edit dialog pre-filled', async () => {
    // given

    // when
    await members.navigateTo();
    await members.getEditButton('Jane Smith').click();

    // then
    await expect(members.getDialog()).toHaveCount(1);
    await expect(members.getDialogTitle()).toHaveAttribute('transloco', 'member.edit.title');
    await expect(members.getDialog().locator('input').first()).toHaveValue('janesmith');
  });

  test('should update the role on save', async ({ page }) => {
    // given
    await mockApi(page, '/api/member', 'johndoe/p1/member-updated', { method: 'PUT' });

    // when
    await members.navigateTo();
    await members.getEditButton('Jane Smith').click();
    await members.selectMemberRole('Manager');
    const updateMember = waitForApi(page, '/api/member', 'PUT');
    await members.getDialogSaveButton().click();

    // then
    await updateMember;
    await expect(members.getDialog()).toHaveCount(0);
    await expect(members.getMemberRow('Jane Smith')).toContainText('Manager');
  });

  test('should hide the dialog on cancel', async () => {
    // given

    // when
    await members.navigateTo();
    await members.getEditButton('Jane Smith').click();
    await members.getDialogCancelButton().click();

    // then
    await expect(members.getDialog()).toHaveCount(0);
  });
});
