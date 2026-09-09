import { test, expect } from '../../../support/test';
import { mockApi, remockApi, waitForApi } from '../../../support/mock-api';
import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

test.describe('Project Members Delete Member', () => {
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

  test('should show the delete button on a non-owner row', async () => {
    // given

    // when
    await members.navigateTo();

    // then
    await expect(members.getDeleteButton('Ronny Lee')).toHaveCount(1);
  });

  test('should not show a delete button on the sole Owner row', async () => {
    // given

    // when
    await members.navigateTo();

    // then
    await expect(members.getMemberRow('John Doe').locator('confirm-button.delete')).toHaveCount(0);
  });

  test('should show the confirmation menu on clicking delete', async ({ page }) => {
    // given

    // when
    await members.navigateTo();
    await members.getDeleteButton('Ronny Lee').click();

    // then
    await expect(page.locator('.mat-mdc-menu-panel button.confirm')).toHaveText('Remove');
  });

  test('should remove the row after confirming delete', async ({ page }) => {
    // given
    await members.navigateTo();
    await expect(members.getMemberRows()).toHaveCount(4);
    await mockApi(page, '/api/member/*', 'johndoe/p1/member-deleted', { method: 'DELETE' });
    await remockApi(page, '/api/project/*/members*', 'johndoe/p1/members-minus-ronny');

    // when
    await members.getDeleteButton('Ronny Lee').click();
    const deleteMember = waitForApi(page, '/api/member/*', 'DELETE');
    await page.locator('.mat-mdc-menu-panel button.confirm').click();

    // then
    await deleteMember;
    await expect(members.getMemberRow('Ronny Lee')).toHaveCount(0);
    await expect(members.getMemberRows()).toHaveCount(3);
  });
});
