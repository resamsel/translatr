import { test, expect } from '../../../support/test';
import { mockApi, remockApi, waitForApi } from '../../../support/mock-api';
import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

test.describe('Project Members Add Member', () => {
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

  test('should show the add member FAB', async () => {
    // given

    // when
    await members.navigateTo();

    // then
    await expect(members.getFloatingActionButton()).toBeVisible();
  });

  test('should open the add member dialog on FAB click', async () => {
    // given

    // when
    await members.navigateTo();
    await members.getFloatingActionButton().click();

    // then
    await expect(members.getDialog()).toHaveCount(1);
    await expect(members.getDialogTitle()).toHaveAttribute('transloco', 'member.create.title');
  });

  test('should hide the dialog on cancel', async () => {
    // given

    // when
    await members.navigateTo();
    await members.getFloatingActionButton().click();
    await members.getDialogCancelButton().click();

    // then
    await expect(members.getDialog()).toHaveCount(0);
  });

  test('should add the member and show the new row on save', async ({ page }) => {
    // given
    await mockApi(page, '/api/member', 'johndoe/p1/member-created', { method: 'POST' });
    await mockApi(page, '/api/users*', 'johndoe/p1/users-mika');
    await remockApi(page, '/api/project/*/members*', 'johndoe/p1/members-added');

    // when
    await members.navigateTo();
    await members.getFloatingActionButton().click();
    await members.fillMemberUser('mika');
    await members.selectMemberRole('Developer');
    const createMember = waitForApi(page, '/api/member', 'POST');
    await members.getDialogSaveButton().click();

    // then
    await createMember;
    await expect(members.getDialog()).toHaveCount(0);
    await expect(members.getMemberRow('Mika Novak').first()).toBeVisible();
    await expect(members.getMemberRow('Mika Novak').first()).toContainText('Developer');
  });
});
