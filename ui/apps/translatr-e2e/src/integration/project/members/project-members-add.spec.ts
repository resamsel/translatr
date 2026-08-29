import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

describe('Project Members Add Member', () => {
  let page: ProjectMembersPage;

  beforeEach(() => {
    page = new ProjectMembersPage('johndoe', 'p1');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1' });
    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales' });
    cy.intercept('/api/project/*/keys*', { fixture: 'johndoe/p1/keys' });
    cy.intercept('/api/project/*/messages*', { fixture: 'johndoe/p1/messages' });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members' });
    cy.intercept('/api/project/*/activities*', { fixture: 'johndoe/p1/activities' });
    cy.intercept('/api/activities/aggregated*', { fixture: 'johndoe/p1/activities-aggregated' });
  });

  it('should show the add member FAB', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getFloatingActionButton().should('be.visible');
  });

  it('should open the add member dialog on FAB click', () => {
    // given

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();

    // then
    page.getDialog().should('have.length', 1);
    page
      .getDialogTitle()
      .should('have.attr', 'transloco', 'member.create.title');
  });

  it('should hide the dialog on cancel', () => {
    // given

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();
    page.getDialogCancelButton().click();

    // then
    page.getDialog().should('have.length', 0);
  });

  it('should add the member and show the new row on save', () => {
    // given
    cy.intercept('POST', '/api/member', { fixture: 'johndoe/p1/member-created' }).as('createMember');
    cy.intercept('/api/users*', { fixture: 'johndoe/p1/users-mika' });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members-added' });

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();
    page.fillMemberUser('mika');
    page.selectMemberRole('Developer');
    page.getDialogSaveButton().click();

    // then
    cy.wait('@createMember');
    page.getDialog().should('have.length', 0);
    page.getMemberRow('Mika Novak').should('exist');
    page.getMemberRow('Mika Novak').should('contain.text', 'Developer');
  });
});
