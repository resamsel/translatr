import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

describe('Project Members Transfer Ownership', () => {
  let page: ProjectMembersPage;

  beforeEach(() => {
    page = new ProjectMembersPage('johndoe', 'p1');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1' });
    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales' });
    cy.intercept('/api/project/*/keys*', { fixture: 'johndoe/p1/keys' });
    cy.intercept('/api/project/*/messages*', { fixture: 'johndoe/p1/messages' });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members-two-owners' });
    cy.intercept('/api/project/*/activities*', { fixture: 'johndoe/p1/activities' });
    cy.intercept('/api/activities/aggregated*', { fixture: 'johndoe/p1/activities-aggregated' });
  });

  it('should show the transfer-ownership button on an owner row when two owners exist', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getMemberRow('John Doe')
      .find('button.edit mat-icon')
      .should('contain.text', 'swap_horiz');
  });

  it('should open the owner edit dialog on clicking the transfer button', () => {
    // given

    // when
    page.navigateTo();
    page.getMemberRow('John Doe').find('button.edit').click();

    // then
    page.getDialog().should('have.length', 1);
    page
      .getDialogTitle()
      .should('have.attr', 'transloco', 'project.transferOwnership.title');
  });

  it('should transfer ownership and return to the members page on save', () => {
    // given
    cy.intercept('/api/users*', { fixture: 'johndoe/p1/users-mika' });
    cy.intercept('PUT', '/api/project', { fixture: 'johndoe/p1' }).as('updateProject');

    // when
    page.navigateTo();
    page.getMemberRow('John Doe').find('button.edit').click();
    page
      .getDialog()
      .find('input')
      .first()
      .clear()
      .type('ronnylee');
    cy.get('mat-option').contains('ronnylee').click();
    page.getDialogSaveButton().click();

    // then
    cy.wait('@updateProject');
    page.getDialog().should('have.length', 0);
    cy.url().should('include', '/johndoe/p1/members');
  });
});
