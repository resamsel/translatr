import { test, expect } from '../../../support/test';
import { mockApi } from '../../../support/mock-api';
import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

test.describe('Project Members', () => {
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

  test('should have page name johndoe/p1', async () => {
    // given

    // when
    await members.navigateTo();

    // then
    await expect(members.getPageName()).toHaveText('johndoe/p1');
  });

  test('should render all four members with name and role', async () => {
    // given

    // when
    await members.navigateTo();

    // then
    await expect(members.getMemberRows()).toHaveCount(4);
    await expect(members.getMemberRow('John Doe')).toContainText('Owner');
    await expect(members.getMemberRow('Jane Smith')).toContainText('Developer');
    await expect(members.getMemberRow("Sophia O'Reilly")).toContainText('Translator');
    await expect(members.getMemberRow('Ronny Lee')).toContainText('Manager');
  });
});
